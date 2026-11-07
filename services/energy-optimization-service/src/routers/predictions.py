from datetime import datetime
from fastapi import APIRouter, HTTPException, Depends
from src.models.schemas import (
    DemandPredictionRequest,
    DemandPredictionResponse,
    RecommendedTimeSlot,
    PeakHourAnalysis,
    ChargingRecommendation,
    StationRecommendation,
    BatchPredictionRequest,
    BatchPredictionResponse
)
from src.ml.demand_predictor import DemandPredictor
from src.config import get_settings

router = APIRouter(prefix="/api/v1/energy/predictions", tags=["Predictions"])

predictor: DemandPredictor = None


def get_predictor() -> DemandPredictor:
    global predictor
    if predictor is None:
        settings = get_settings()
        predictor = DemandPredictor(settings.model_path)
        predictor.load_or_train()
    return predictor


@router.post("/demand", response_model=DemandPredictionResponse)
def predict_demand(req: DemandPredictionRequest, dp: DemandPredictor = Depends(get_predictor)):
    try:
        dt = datetime.fromisoformat(req.datetime.replace("Z", "+00:00"))
        features = dp.extract_features(dt, req.station_id)
        pred_kw, conf = dp.predict(features)

        hour = dt.hour
        is_peak = (8 <= hour <= 10) or (18 <= hour <= 21)
        peak_prob = 0.85 if is_peak else 0.25

        rec_times = [
            RecommendedTimeSlot(time="02:00", load_level="low", predicted_kw=round(pred_kw * 0.2, 2)),
            RecommendedTimeSlot(time="13:00", load_level="medium", predicted_kw=round(pred_kw * 0.6, 2)),
            RecommendedTimeSlot(time="23:00", load_level="low", predicted_kw=round(pred_kw * 0.3, 2))
        ]

        return DemandPredictionResponse(
            station_id=req.station_id,
            predicted_demand_kw=round(pred_kw, 2),
            confidence_score=round(conf, 2),
            peak_probability=peak_prob,
            recommended_times=rec_times
        )
    except Exception as e:
        raise HTTPException(status_code=400, detail=f"Prediction failed: {str(e)}")


@router.get("/peak-hours/{station_id}", response_model=PeakHourAnalysis)
def get_peak_hours(station_id: str):
    return PeakHourAnalysis(
        station_id=station_id,
        date=datetime.now().strftime("%Y-%m-%d"),
        peak_hours=[8, 9, 10, 18, 19, 20, 21],
        off_peak_hours=[0, 1, 2, 3, 4, 5, 12, 13, 14, 23],
        avg_peak_demand_kw=82.5,
        avg_off_peak_demand_kw=18.4
    )


@router.get("/recommendations", response_model=ChargingRecommendation)
def get_recommendations(lat: float = 19.0760, lng: 72.8777):
    stations = [
        StationRecommendation(
            station_id="a1111111-1111-1111-1111-111111111101",
            station_name="Tata Power EZ Charge - BKC Hub",
            distance_km=2.4,
            available_connectors=3,
            estimated_wait_minutes=0,
            demand_score=0.45,
            recommendation_score=0.92
        ),
        StationRecommendation(
            station_id="a1111111-1111-1111-1111-111111111110",
            station_name="Magenta ChargeGrid - Inorbit Mall Vashi",
            distance_km=8.1,
            available_connectors=2,
            estimated_wait_minutes=5,
            demand_score=0.60,
            recommendation_score=0.78
        )
    ]
    return ChargingRecommendation(
        user_location_lat=lat,
        user_location_lng=lng,
        recommended_stations=stations
    )


@router.post("/batch", response_model=BatchPredictionResponse)
def batch_predict(batch: BatchPredictionRequest, dp: DemandPredictor = Depends(get_predictor)):
    results = []
    errors = []
    for r in batch.requests:
        try:
            dt = datetime.fromisoformat(r.datetime.replace("Z", "+00:00"))
            features = dp.extract_features(dt, r.station_id)
            kw, conf = dp.predict(features)
            results.append(DemandPredictionResponse(
                station_id=r.station_id,
                predicted_demand_kw=round(kw, 2),
                confidence_score=round(conf, 2),
                peak_probability=0.5,
                recommended_times=[]
            ))
        except Exception as e:
            errors.append(f"Station {r.station_id}: {str(e)}")

    return BatchPredictionResponse(
        results=results,
        total_processed=len(results),
        errors=errors
    )
