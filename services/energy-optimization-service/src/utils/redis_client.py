import json
import logging
from typing import Optional, Any
import redis

logger = logging.getLogger(__name__)


class RedisClient:
    def __init__(self, redis_url: str):
        self.client: Optional[redis.Redis] = None
        try:
            self.client = redis.from_url(redis_url, decode_responses=True)
            self.client.ping()
            logger.info("Connected to Redis successfully.")
        except Exception as e:
            logger.warn(f"Redis not available ({e}), fallback to in-memory/no cache.")
            self.client = None

    def get(self, key: str) -> Optional[Any]:
        if not self.client:
            return None
        try:
            val = self.client.get(key)
            return json.loads(val) if val else None
        except Exception:
            return None

    def set(self, key: str, value: Any, ttl: int = 3600) -> bool:
        if not self.client:
            return False
        try:
            self.client.setex(key, ttl, json.dumps(value))
            return True
        except Exception:
            return False
