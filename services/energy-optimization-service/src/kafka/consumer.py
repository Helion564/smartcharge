import json
import logging
import threading
from typing import Optional
from kafka import KafkaConsumer
from src.config import get_settings

logger = logging.getLogger(__name__)


class EnergyKafkaConsumer:
    def __init__(self):
        self.settings = get_settings()
        self.running = False
        self.thread: Optional[threading.Thread] = None

    def start(self):
        self.running = True
        self.thread = threading.Thread(target=self._run, daemon=True)
        self.thread.start()
        logger.info("Started Energy Kafka Consumer background thread.")

    def stop(self):
        self.running = False

    def _run(self):
        try:
            consumer = KafkaConsumer(
                self.settings.kafka_session_topic,
                bootstrap_servers=self.settings.kafka_bootstrap_servers.split(","),
                group_id=self.settings.kafka_consumer_group,
                auto_offset_reset="earliest",
                value_deserializer=lambda m: json.loads(m.decode("utf-8")),
                consumer_timeout_ms=1000
            )

            while self.running:
                for msg in consumer:
                    if not self.running:
                        break
                    try:
                        data = msg.value
                        event_type = data.get("eventType")
                        station_id = data.get("stationId")
                        if event_type == "SESSION_COMPLETED":
                            logger.info(f"Recorded completed session for station {station_id} for ML re-training.")
                    except Exception as ex:
                        logger.warn(f"Error handling Kafka message: {ex}")

            consumer.close()
        except Exception as e:
            logger.warn(f"Kafka consumer could not connect to {self.settings.kafka_bootstrap_servers}: {e}")
