from contextlib import asynccontextmanager
import logging
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from src.config import get_settings
from src.routers import health, predictions, optimization, analytics
from src.ml.demand_predictor import DemandPredictor
from src.kafka.consumer import EnergyKafkaConsumer

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("energy-service")

kafka_consumer = EnergyKafkaConsumer()


@asynccontextmanager
async def lifespan(app: FastAPI):
    settings = get_settings()
    logger.info("Initializing Energy Optimization Service & ML Model...")
    dp = DemandPredictor(settings.model_path)
    dp.load_or_train()
    kafka_consumer.start()
    yield
    kafka_consumer.stop()
    logger.info("Shutting down Energy Optimization Service...")


app = FastAPI(
    title="SmartCharge - Energy Optimization & AI Service",
    description="Microservice providing Demand Prediction, Peak Hour Analysis, and Smart Energy Distribution for EV Charging",
    version="1.0.0",
    lifespan=lifespan
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(health.router)
app.include_router(predictions.router)
app.include_router(optimization.router)
app.include_router(analytics.router)


@app.get("/")
def root():
    return {
        "service": "SmartCharge Energy Optimization Engine",
        "status": "ONLINE",
        "docs": "/docs"
    }
