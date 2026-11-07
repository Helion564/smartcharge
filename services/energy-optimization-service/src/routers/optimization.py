from fastapi import APIRouter
from src.models.schemas import (
    EnergyOptimizationRequest,
    EnergyOptimizationResponse
)
from src.ml.optimizer import EnergyOptimizer

router = APIRouter(prefix="/api/v1/energy/optimization", tags=["Optimization"])

optimizer = EnergyOptimizer()


@router.post("/allocate", response_model=EnergyOptimizationResponse)
def allocate_power(req: EnergyOptimizationRequest):
    sessions_data = [
        {
            "session_id": s.session_id,
            "connector_id": s.connector_id,
            "current_power_kw": s.current_power_kw,
            "estimated_remaining_kwh": s.estimated_remaining_kwh,
            "priority": s.priority,
            "connector_max_kw": s.connector_max_kw
        }
        for s in req.active_sessions
    ]

    return optimizer.optimize_power_allocation(
        station_id=req.station_id,
        total_capacity_kw=req.available_capacity_kw,
        sessions=sessions_data,
        strategy=req.strategy
    )


@router.get("/station/{station_id}/current")
def get_current_optimization(station_id: str):
    return {
        "station_id": station_id,
        "status": "OPTIMIZED",
        "current_load_kw": 48.5,
        "grid_limit_kw": 100.0,
        "efficiency_score": 0.94,
        "active_controlled_sessions": 2
    }

