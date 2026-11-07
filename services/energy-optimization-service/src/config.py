from pydantic_settings import BaseSettings
from functools import lru_cache


class Settings(BaseSettings):
    database_url: str = "sqlite:///./energy_data.db"
    redis_url: str = "redis://localhost:6379"
    kafka_bootstrap_servers: str = "localhost:9092"
    booking_service_url: str = "http://localhost:8083"
    session_service_url: str = "http://localhost:8084"
    model_path: str = "./models/demand_predictor.pkl"
    port: int = 8087
    app_name: str = "SmartCharge Energy Optimization Service"
    app_version: str = "1.0.0"
    debug: bool = False
    kafka_session_topic: str = "session-events"
    kafka_energy_topic: str = "energy-events"
    kafka_consumer_group: str = "energy-optimization-group"
    prediction_cache_ttl: int = 3600   # 1 hour
    optimization_cache_ttl: int = 60   # 60 seconds

    model_config = {"env_file": ".env", "case_sensitive": False}


@lru_cache()
def get_settings() -> Settings:
    return Settings()
