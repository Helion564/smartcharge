"""
Energy Optimizer - Algorithm module for distributing charging power among active sessions.
"""
from __future__ import annotations

import logging
from typing import Any
from src.models.schemas import (
    PowerAllocation,
    EnergyOptimizationResponse,
    DemandCurvePoint
)

logger = logging.getLogger(__name__)


class EnergyOptimizer:
    """
    Intelligent power distribution optimizer based on station grid capacity,
    connector limits, vehicle requirements, and configurable strategies.
    """

    MIN_POWER_KW = 3.3  # Minimum charging power to keep session alive

    def optimize_power_allocation(
        self,
        station_id: str,
        total_capacity_kw: float,
        sessions: list[dict[str, Any]],
        strategy: str = "balanced"
    ) -> EnergyOptimizationResponse:
        """
        Distribute available capacity among active charging sessions.
        
        Strategies:
        - balanced: Distribute power proportionally to connector limit
        - priority: Weight allocations by session priority (1-5)
        - fastest: Allocate maximum possible power to sessions closest to completion
        - economic: Maximize throughput and capacity utilization
        """
        if not sessions:
            return EnergyOptimizationResponse(
                station_id=station_id,
                optimized_allocations=[],
                total_allocated_kw=0.0,
                efficiency_score=1.0,
                strategy_used=strategy
            )

        n = len(sessions)
        min_required = n * self.MIN_POWER_KW

        # If total capacity is less than minimum needed, allocate evenly
        if total_capacity_kw < min_required:
            alloc_kw = total_capacity_kw / n
            allocations = [
                PowerAllocation(
                    session_id=s["session_id"],
                    allocated_power_kw=round(alloc_kw, 2),
                    charge_time_estimate_minutes=self._calc_time(s["estimated_remaining_kwh"], alloc_kw)
                )
                for s in sessions
            ]
            return EnergyOptimizationResponse(
                station_id=station_id,
                optimized_allocations=allocations,
                total_allocated_kw=round(total_capacity_kw, 2),
                efficiency_score=0.5,
                strategy_used=strategy
            )

        allocations: list[PowerAllocation] = []

        if strategy == "priority":
            total_priority = sum(s.get("priority", 3) for s in sessions)
            for s in sessions:
                p = s.get("priority", 3)
                weight = p / total_priority
                raw_alloc = total_capacity_kw * weight
                max_kw = s.get("connector_max_kw", 50.0)
                alloc_kw = max(self.MIN_POWER_KW, min(raw_alloc, max_kw))
                allocations.append(PowerAllocation(
                    session_id=s["session_id"],
                    allocated_power_kw=round(alloc_kw, 2),
                    charge_time_estimate_minutes=self._calc_time(s["estimated_remaining_kwh"], alloc_kw)
                ))

        elif strategy == "fastest":
            # Sort sessions by remaining kWh ascending (closest to full get max power)
            sorted_sessions = sorted(sessions, key=lambda x: x["estimated_remaining_kwh"])
            remaining_cap = total_capacity_kw
            for s in sorted_sessions:
                max_kw = s.get("connector_max_kw", 50.0)
                alloc = min(max_kw, max(self.MIN_POWER_KW, remaining_cap / max(1, len(sorted_sessions))))
                remaining_cap = max(0.0, remaining_cap - alloc)
                allocations.append(PowerAllocation(
                    session_id=s["session_id"],
                    allocated_power_kw=round(alloc, 2),
                    charge_time_estimate_minutes=self._calc_time(s["estimated_remaining_kwh"], alloc)
                ))

        else:  # balanced or economic
            total_max = sum(s.get("connector_max_kw", 50.0) for s in sessions)
            for s in sessions:
                max_kw = s.get("connector_max_kw", 50.0)
                share = (max_kw / total_max) if total_max > 0 else (1.0 / n)
                alloc_kw = min(max_kw, max(self.MIN_POWER_KW, total_capacity_kw * share))
                allocations.append(PowerAllocation(
                    session_id=s["session_id"],
                    allocated_power_kw=round(alloc_kw, 2),
                    charge_time_estimate_minutes=self._calc_time(s["estimated_remaining_kwh"], alloc_kw)
                ))

        total_allocated = sum(a.allocated_power_kw for a in allocations)
        # Scale down if exceeded capacity
        if total_allocated > total_capacity_kw:
            scale = total_capacity_kw / total_allocated
            for a in allocations:
                a.allocated_power_kw = round(max(self.MIN_POWER_KW, a.allocated_power_kw * scale), 2)
            total_allocated = sum(a.allocated_power_kw for a in allocations)

        efficiency = min(1.0, round(total_allocated / total_capacity_kw, 3)) if total_capacity_kw > 0 else 0.0

        return EnergyOptimizationResponse(
            station_id=station_id,
            optimized_allocations=allocations,
            total_allocated_kw=round(total_allocated, 2),
            efficiency_score=efficiency,
            strategy_used=strategy
        )

    def generate_demand_curve(self, station_id: str, base_kw: float = 75.0) -> list[DemandCurvePoint]:
        """Generate a 24-hour demand curve based on Indian EV charging profile."""
        points = []
        # Indian diurnal curve: peaks at 8-10 AM (office arrival) and 6-9 PM (evening/return)
        hourly_multipliers = [
            0.15, 0.10, 0.08, 0.08, 0.12, 0.25,  # 00:00 - 05:00
            0.45, 0.85, 1.25, 1.15, 0.80, 0.65,  # 06:00 - 11:00
            0.60, 0.65, 0.70, 0.75, 0.90, 1.35,  # 12:00 - 17:00
            1.40, 1.20, 0.95, 0.70, 0.45, 0.25   # 18:00 - 23:00
        ]
        for hour, mult in enumerate(hourly_multipliers):
            pred = round(base_kw * mult, 2)
            actual = round(pred * (0.95 + 0.1 * ((hour * 7) % 5) / 5), 2)
            points.append(DemandCurvePoint(hour=hour, predicted_kw=pred, actual_kw=actual))
        return points

    def _calc_time(self, remaining_kwh: float, power_kw: float) -> float:
        if power_kw <= 0:
            return 999.0
        return round((remaining_kwh / power_kw) * 60, 1)

