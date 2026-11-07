from datetime import datetime
from fastapi import APIRouter
from src.models.schemas import (
    StationAnalytics,
    SystemOverview,
    DemandCurvePoint,
    CarbonSavingsReport
)
from src.ml.optimizer import EnergyOptimizer

router = APIRouter(prefix="/api/v1/energy/analytics", tags=["Analytics"])

optimizer = EnergyOptimizer()


@router.get("/station/{station_id}", response_model=StationAnalytics)
def get_station_analytics(station_id: str, period_days: int = 30):
    return StationAnalytics(
        station_id=station_id,
        period_days=period_days,
        total_energy_kwh=12450.0,
        total_sessions=380,
        avg_session_duration_min=42.5,
        peak_hours=[8, 9, 10, 18, 19, 20],
        utilization_rate=0.78
    )


@router.get("/overview", response_model=SystemOverview)
def get_system_overview():
    top = [
        StationAnalytics(
            station_id="a1111111-1111-1111-1111-111111111101",
            period_days=30,
            total_energy_kwh=18900.0,
            total_sessions=520,
            avg_session_duration_min=45.0,
            peak_hours=[8, 9, 10, 18, 19, 20],
            utilization_rate=0.86
        ),
        StationAnalytics(
            station_id="a1111111-1111-1111-1111-111111111102",
            period_days=30,
            total_energy_kwh=16400.0,
            total_sessions=440,
            avg_session_duration_min=38.0,
            peak_hours=[8, 9, 10, 18, 19, 20],
            utilization_rate=0.81
        )
    ]

    return SystemOverview(
        total_stations=10,
        total_energy_kwh=85600.0,
        total_sessions=2450,
        avg_utilization_rate=0.74,
        top_stations=top,
        generated_at=datetime.now().isoformat()
    )


@router.get("/demand-curve/{station_id}", response_model=list[DemandCurvePoint])
def get_demand_curve(station_id: str):
    return optimizer.generate_demand_curve(station_id)


@router.get("/carbon-savings", response_model=CarbonSavingsReport)
def get_carbon_savings(period_days: int = 30):
    total_energy = 85600.0
    # Average EV saves ~0.7 kg CO2 per kWh vs petrol car (Indian grid factor adjusted)
    co2_saved = round(total_energy * 0.72, 2)
    trees = round(co2_saved / 21.77, 1)  # 1 mature tree absorbs ~21.77 kg CO2/year
    petrol_litres = round(total_energy * 0.28, 1)

    return CarbonSavingsReport(
        period_days=period_days,
        total_energy_kwh=total_energy,
        co2_saved_kg=co2_saved,
        trees_equivalent=trees,
        petrol_saved_litres=petrol_litres,
        generated_at=datetime.now().isoformat()
    )

