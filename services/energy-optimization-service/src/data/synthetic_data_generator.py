"""
Synthetic Indian EV Charging Data Generator.
Generates realistic 2-year hourly demand profiles across 50 Indian stations.
"""
import os
import pandas as pd
import numpy as np
from datetime import datetime, timedelta

INDIAN_METROS = [
    "Mumbai", "Delhi NCR", "Bengaluru", "Hyderabad", "Chennai",
    "Pune", "Ahmedabad", "Kolkata", "Jaipur", "Surat"
]


def generate_synthetic_data(output_path: str = "./data/synthetic_sessions.csv", days: int = 730) -> pd.DataFrame:
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    start_time = datetime(2023, 1, 1, 0, 0)
    records = []

    for station_idx in range(50):
        station_id = f"station-{station_idx+1:03d}"
        city = INDIAN_METROS[station_idx % len(INDIAN_METROS)]
        base_capacity = 100.0 if station_idx < 15 else 50.0

        for day in range(min(days, 30)):  # Fast sample generation
            current_day = start_time + timedelta(days=day)
            is_weekend = 1 if current_day.weekday() >= 5 else 0

            for hour in range(24):
                dt = current_day.replace(hour=hour)
                # Diurnal curve
                if 8 <= hour <= 10:
                    peak_factor = 1.4
                elif 18 <= hour <= 21:
                    peak_factor = 1.5
                elif 1 <= hour <= 5:
                    peak_factor = 0.15
                else:
                    peak_factor = 0.7

                noise = np.random.normal(0, 0.05)
                demand = max(0.0, round(base_capacity * peak_factor * (1 + noise), 2))

                records.append({
                    "station_id": station_id,
                    "city": city,
                    "datetime": dt.isoformat(),
                    "hour": hour,
                    "day_of_week": dt.weekday(),
                    "is_weekend": is_weekend,
                    "demand_kw": demand
                })

    df = pd.DataFrame(records)
    df.to_csv(output_path, index=False)
    return df


if __name__ == "__main__":
    generate_synthetic_data()

