from __future__ import annotations

from typing import Optional
from pydantic import BaseModel, Field


# ---------------------------------------------------------------------------
# Demand prediction
# ---------------------------------------------------------------------------

class DemandPredictionRequest(BaseModel):
    station_id: str = Field(..., description="Unique identifier of the charging station")
    datetime: str = Field(..., description="ISO-8601 datetime for which to predict demand")
    connector_type: Optional[str] = Field(None, description="Connector type (CCS, CHAdeMO, Type-2, etc.)")


class RecommendedTimeSlot(BaseModel):
    time: str          # e.g. "14:00"
    load_level: str    # "low" | "medium" | "high"
    predicted_kw: float


class DemandPredictionResponse(BaseModel):
    station_id: str
    predicted_demand_kw: float
    confidence_score: float = Field(..., ge=0.0, le=1.0)
    peak_probability: float = Field(..., ge=0.0, le=1.0)
    recommended_times: list[RecommendedTimeSlot]


# ---------------------------------------------------------------------------
# Energy optimisation
# ---------------------------------------------------------------------------

class SessionInfo(BaseModel):
    session_id: str
    connector_id: str
    current_power_kw: float
    estimated_remaining_kwh: float
    priority: int = Field(default=3, ge=1, le=5)
    connector_max_kw: float = Field(default=50.0, description="Connector rated capacity")


class EnergyOptimizationRequest(BaseModel):
    station_id: str
    available_capacity_kw: float = Field(..., gt=0)
    active_sessions: list[SessionInfo]
    strategy: str = Field(
        default="balanced",
        description="balanced | priority | fastest | economic",
    )


class PowerAllocation(BaseModel):
    session_id: str
    allocated_power_kw: float
    charge_time_estimate_minutes: float


class EnergyOptimizationResponse(BaseModel):
    station_id: str
    optimized_allocations: list[PowerAllocation]
    total_allocated_kw: float
    efficiency_score: float = Field(..., ge=0.0, le=1.0)
    strategy_used: str


# ---------------------------------------------------------------------------
# Analytics
# ---------------------------------------------------------------------------

class PeakHourAnalysis(BaseModel):
    station_id: str
    date: str
    peak_hours: list[int]        # hours 0-23
    off_peak_hours: list[int]
    avg_peak_demand_kw: float
    avg_off_peak_demand_kw: float


class StationAnalytics(BaseModel):
    station_id: str
    period_days: int
    total_energy_kwh: float
    total_sessions: int
    avg_session_duration_min: float
    peak_hours: list[int]
    utilization_rate: float = Field(..., ge=0.0, le=1.0)


class SystemOverview(BaseModel):
    total_stations: int
    total_energy_kwh: float
    total_sessions: int
    avg_utilization_rate: float
    top_stations: list[StationAnalytics]
    generated_at: str


class DemandCurvePoint(BaseModel):
    hour: int
    predicted_kw: float
    actual_kw: Optional[float] = None


class CarbonSavingsReport(BaseModel):
    period_days: int
    total_energy_kwh: float
    co2_saved_kg: float          # vs ICE vehicles
    trees_equivalent: float
    petrol_saved_litres: float
    generated_at: str


# ---------------------------------------------------------------------------
# Recommendations
# ---------------------------------------------------------------------------

class StationRecommendation(BaseModel):
    station_id: str
    station_name: str
    distance_km: float
    available_connectors: int
    estimated_wait_minutes: int
    demand_score: float          # 0-1 (lower = less busy)
    recommendation_score: float  # composite score (higher = better)


class ChargingRecommendation(BaseModel):
    user_location_lat: float
    user_location_lng: float
    recommended_stations: list[StationRecommendation]


# ---------------------------------------------------------------------------
# Batch predictions
# ---------------------------------------------------------------------------

class BatchPredictionRequest(BaseModel):
    requests: list[DemandPredictionRequest]


class BatchPredictionResponse(BaseModel):
    results: list[DemandPredictionResponse]
    total_processed: int
    errors: list[str] = []
