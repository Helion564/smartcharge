from fastapi import APIRouter
from pydantic import BaseModel

router = APIRouter(prefix="/health", tags=["Health"])


class HealthStatus(BaseModel):
    status: str
    service: str
    version: str


@router.get("", response_model=HealthStatus)
def health_check():
    return HealthStatus(
        status="UP",
        service="energy-optimization-service",
        version="1.0.0"
    )

