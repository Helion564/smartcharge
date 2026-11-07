"""
Demand Predictor – Random Forest model for EV charging demand.

Trains on synthetic Indian EV charging patterns when no persisted model
exists, or loads a previously saved model from disk.
"""
from __future__ import annotations

import logging
import os
import random
from datetime import datetime, timedelta
from pathlib import Path
from typing import Optional

import joblib
import numpy as np
import pandas as pd
from sklearn.ensemble import RandomForestRegressor
from sklearn.metrics import mean_absolute_error, r2_score
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler

logger = logging.getLogger(__name__)

# ---------------------------------------------------------------------------
# Indian public holidays (month, day) – fixed dates only
# Approximate set; Diwali / Holi etc. shift yearly but we use a fixed proxy
# ---------------------------------------------------------------------------
INDIAN_HOLIDAYS: set[tuple[int, int]] = {
    (1, 26),   # Republic Day
    (3, 22),   # Holi proxy
    (4, 14),   # Ambedkar Jayanti / Tamil New Year
    (8, 15),   # Independence Day
    (10, 2),   # Gandhi Jayanti
    (11, 1),   # Diwali proxy
    (12, 25),  # Christmas
}

# Indian city-tier mapping (station index → tier factor)
# Tier-1 metros have higher overall demand; Tier-2 have flatter curves
CITY_TIER_FACTORS = [1.3] * 15 + [1.0] * 20 + [0.75] * 15   # 50 stations


class DemandPredictor:
    """Train or load a Random Forest demand predictor."""

    FEATURE_NAMES = [
        "hour",
        "day_of_week",
        "month",
        "is_weekend",
        "is_holiday",
        "temperature",
        "is_morning_peak",
        "is_evening_peak",
        "station_avg_demand",
        "lag_1h",
        "lag_24h",
        "lag_168h",
    ]

    def __init__(self, model_path: str) -> None:
        self.model_path = model_path
        self.scaler_path = model_path.replace(".pkl", "_scaler.pkl")
        self.model: Optional[RandomForestRegressor] = None
        self.scaler: Optional[StandardScaler] = None
        self._is_ready = False

    # ------------------------------------------------------------------
    # Public API
    # ------------------------------------------------------------------

    def load_or_train(self) -> None:
        """Load model from disk; train from scratch if not found."""
        if os.path.exists(self.model_path) and os.path.exists(self.scaler_path):
            try:
                self.model = joblib.load(self.model_path)
                self.scaler = joblib.load(self.scaler_path)
                self._is_ready = True
                logger.info("Loaded demand predictor from %s", self.model_path)
                return
            except Exception as exc:
                logger.warning("Failed to load model: %s – retraining.", exc)

        logger.info("Training demand predictor on synthetic data …")
        df = self._generate_synthetic_training_data()
        X = df[self.FEATURE_NAMES].values
        y = df["demand_kw"].values
        self.train(X, y)

    def train(self, X: np.ndarray, y: np.ndarray) -> dict:
        """Fit the RF model and persist both model and scaler."""
        X_train, X_test, y_train, y_test = train_test_split(
            X, y, test_size=0.15, random_state=42
        )

        self.scaler = StandardScaler()
        X_train_sc = self.scaler.fit_transform(X_train)
        X_test_sc = self.scaler.transform(X_test)

        self.model = RandomForestRegressor(
            n_estimators=200,
            max_depth=12,
            min_samples_split=5,
            min_samples_leaf=2,
            max_features="sqrt",
            random_state=42,
            n_jobs=-1,
        )
        self.model.fit(X_train_sc, y_train)

        y_pred = self.model.predict(X_test_sc)
        mae = mean_absolute_error(y_test, y_pred)
        r2 = r2_score(y_test, y_pred)
        logger.info("Model trained – MAE=%.3f kW  R²=%.4f", mae, r2)

        # Persist
        Path(self.model_path).parent.mkdir(parents=True, exist_ok=True)
        joblib.dump(self.model, self.model_path)
        joblib.dump(self.scaler, self.scaler_path)
        self._is_ready = True
        logger.info("Model saved to %s", self.model_path)
        return {"mae": mae, "r2": r2}

    def predict(self, features: dict) -> tuple[float, float]:
        """
        Return (predicted_demand_kw, confidence_score ∈ [0,1]).

        Confidence is derived from the spread of individual tree predictions.
        """
        if not self._is_ready:
            raise RuntimeError("Model not loaded – call load_or_train() first.")

        row = np.array([[features.get(f, 0.0) for f in self.FEATURE_NAMES]])
        row_sc = self.scaler.transform(row)

        # Collect individual tree predictions for uncertainty estimation
        tree_preds = np.array([tree.predict(row_sc)[0] for tree in self.model.estimators_])
        pred_mean = float(np.clip(tree_preds.mean(), 0, None))
        pred_std = float(tree_preds.std())

        # Confidence: low variance → high confidence
        # Normalise by the mean to get a coefficient of variation
        cv = pred_std / (pred_mean + 1e-6)
        confidence = float(np.clip(1.0 - min(cv, 1.0), 0.3, 1.0))

        return pred_mean, confidence

    @property
    def is_ready(self) -> bool:
        return self._is_ready

    # ------------------------------------------------------------------
    # Feature engineering
    # ------------------------------------------------------------------

    def extract_features(
        self,
        dt: datetime,
        station_id: str,
        historical_data: list[dict] | None = None,
    ) -> dict:
        """Build a feature dict from a datetime + optional history."""
        h = dt.hour
        dow = dt.weekday()   # 0=Monday
        month = dt.month

        is_weekend = int(dow >= 5)
        is_holiday = int((month, dt.day) in INDIAN_HOLIDAYS)

        # Peak periods for Indian EV charging
        is_morning_peak = int(7 <= h <= 10)
        is_evening_peak = int(17 <= h <= 21)

        # Synthetic temperature (°C): peak in May-Jun, cooler in Dec-Jan
        base_temp = 25 + 10 * np.sin((month - 1) * np.pi / 6)
        temp_variation = -5 * np.cos(h * np.pi / 12)   # hotter in afternoon
        temperature = float(base_temp + temp_variation + random.gauss(0, 1))

        # Station average demand – derive from station_id hash as a proxy
        station_avg = self._station_avg_demand(station_id)

        # Lag features (fall back to station average when no history)
        lag_1h = lag_24h = lag_168h = station_avg
        if historical_data:
            lag_1h = historical_data[-1].get("demand_kw", station_avg) if len(historical_data) >= 1 else station_avg
            lag_24h = historical_data[-24].get("demand_kw", station_avg) if len(historical_data) >= 24 else station_avg
            lag_168h = historical_data[-168].get("demand_kw", station_avg) if len(historical_data) >= 168 else station_avg

        return {
            "hour": h,
            "day_of_week": dow,
            "month": month,
            "is_weekend": is_weekend,
            "is_holiday": is_holiday,
            "temperature": temperature,
            "is_morning_peak": is_morning_peak,
            "is_evening_peak": is_evening_peak,
            "station_avg_demand": station_avg,
            "lag_1h": lag_1h,
            "lag_24h": lag_24h,
            "lag_168h": lag_168h,
        }

    # ------------------------------------------------------------------
    # Synthetic data generation
    # ------------------------------------------------------------------

    def _generate_synthetic_training_data(self) -> pd.DataFrame:
        """
        Generate ~2 years × 50 stations of hourly demand data.
        Indian EV patterns:
          - Morning peak  08:00-10:00
          - Evening peak  18:00-21:00
          - Summer (Apr-Jun) overall higher demand (+15 %)
          - Holidays: flatter weekday-like patterns
          - Tier-1 metros: higher base demand
          - Realistic noise
        """
        rng = np.random.default_rng(2024)
        start = datetime(2023, 1, 1)
        end = datetime(2024, 12, 31, 23)
        timestamps = pd.date_range(start, end, freq="1h")
        n_stations = 50

        records: list[dict] = []

        for s_idx in range(n_stations):
            tier_factor = CITY_TIER_FACTORS[s_idx]
            # Base capacity (kW) for this station: 20–150 kW
            base_cap = rng.uniform(20, 150)
            station_id = f"STATION_{s_idx:03d}"

            prev_demand: list[float] = []

            for ts in timestamps:
                h = ts.hour
                dow = ts.weekday()
                month = ts.month

                is_weekend = dow >= 5
                is_holiday = (month, ts.day) in INDIAN_HOLIDAYS

                # Hourly demand shape (0-1)
                if 7 <= h <= 10:
                    hour_factor = 0.75 + 0.25 * np.sin((h - 7) * np.pi / 3)
                elif 18 <= h <= 21:
                    hour_factor = 0.80 + 0.20 * np.sin((h - 18) * np.pi / 3)
                elif 12 <= h <= 14:
                    hour_factor = 0.45
                elif 0 <= h <= 5:
                    hour_factor = 0.10 + 0.05 * rng.random()
                else:
                    hour_factor = 0.30 + 0.10 * rng.random()

                # Day-of-week
                if is_holiday:
                    dow_factor = 0.70
                elif is_weekend:
                    dow_factor = 0.75 + 0.10 * rng.random()
                else:
                    dow_factor = 0.90 + 0.10 * rng.random()

                # Seasonal (hotter months → more AC/EV demand)
                seasonal = 1.0 + 0.15 * np.sin((month - 1) * np.pi / 6)

                demand = base_cap * hour_factor * dow_factor * seasonal * tier_factor
                demand = max(0.0, demand + rng.normal(0, demand * 0.08))

                lag_1h = prev_demand[-1] if prev_demand else demand
                lag_24h = prev_demand[-24] if len(prev_demand) >= 24 else demand
                lag_168h = prev_demand[-168] if len(prev_demand) >= 168 else demand

                base_temp = 25 + 10 * np.sin((month - 1) * np.pi / 6)
                temp_variation = -5 * np.cos(h * np.pi / 12)
                temperature = base_temp + temp_variation + rng.normal(0, 1)

                records.append(
                    {
                        "station_id": station_id,
                        "hour": h,
                        "day_of_week": dow,
                        "month": month,
                        "is_weekend": int(is_weekend),
                        "is_holiday": int(is_holiday),
                        "temperature": temperature,
                        "is_morning_peak": int(7 <= h <= 10),
                        "is_evening_peak": int(18 <= h <= 21),
                        "station_avg_demand": base_cap * 0.5 * tier_factor,
                        "lag_1h": lag_1h,
                        "lag_24h": lag_24h,
                        "lag_168h": lag_168h,
                        "demand_kw": demand,
                    }
                )
                prev_demand.append(demand)
                if len(prev_demand) > 200:
                    prev_demand.pop(0)

        df = pd.DataFrame(records)
        logger.info(
            "Synthetic dataset: %d rows, %d features",
            len(df),
            len(self.FEATURE_NAMES),
        )
        return df

    # ------------------------------------------------------------------
    # Helpers
    # ------------------------------------------------------------------

    def _station_avg_demand(self, station_id: str) -> float:
        """Derive a stable 'average demand' proxy from the station_id string."""
        h = abs(hash(station_id)) % 1000
        return 10.0 + h * 0.14    # yields values in [10, 150]
