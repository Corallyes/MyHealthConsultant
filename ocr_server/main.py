import base64
import io
import logging

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from rapidocr import RapidOCR

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="RapidOCR Drug Recognition API")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

engine = RapidOCR()


class OcrRequest(BaseModel):
    image: str  # base64 encoded image


class OcrResponse(BaseModel):
    texts: list[str]
    scores: list[float]
    success: bool
    error: str | None = None


@app.get("/api/health")
async def health():
    return {"status": "ok"}


@app.post("/api/ocr", response_model=OcrResponse)
async def recognize_image(req: OcrRequest):
    try:
        image_bytes = base64.b64decode(req.image)
        result = engine(image_bytes)

        if result is None or result.txts is None:
            return OcrResponse(texts=[], scores=[], success=True)

        texts = list(result.txts)
        scores = [float(s) for s in result.scores] if result.scores else []
        logger.info("OCR recognized %d text segments", len(texts))
        return OcrResponse(texts=texts, scores=scores, success=True)

    except Exception as e:
        logger.error("OCR failed: %s", e)
        return OcrResponse(texts=[], scores=[], success=False, error=str(e))


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
