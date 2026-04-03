import httpx, asyncio, os
from services.config.firebase_config import db
from dotenv import load_dotenv

load_dotenv()

GOOGLE_KEY = os.getenv("GOOGLE_API_KEY")
UNSPLASH_KEY = os.getenv("UNSPLASH_API_KEY")



# place photos from Google Places API
async def get_google_images(city):
    url = f"https://maps.googleapis.com/maps/api/place/textsearch/json?query={city}&key={GOOGLE_KEY}"
    async with httpx.AsyncClient() as client:
        res = await client.get(url)
    data = res.json()
    
    photos = []
    if data["results"]:
        place = data["results"][0]
        if "photos" in place:
            for p in place["photos"][:2]:
                ref = p["photo_reference"]
                photo_url = f"https://maps.googleapis.com/maps/api/place/photo?maxwidth=800&photo_reference={ref}&key={GOOGLE_KEY}"
                photos.append(photo_url)
    
    return photos


# place photos from Unsplash API
async def get_unsplash_images(city):
    url = f"https://api.unsplash.com/search/photos?query={city}&per_page=2&client_id={UNSPLASH_KEY}"
    async with httpx.AsyncClient() as client:
        res = await client.get(url)
    data = res.json()
    
    photos = []
    for p in data["results"]:
        photos.append(p["urls"]["regular"])
        
    return photos


# get images for a place
async def get_place_images(city:str, lat:float, lng:float):
    doc_id = f"{lat}_{lng}"
    doc_ref = db.collection("Places").document(doc_id)
    doc = doc_ref.get()
    
    if doc.exists:
        return doc.to_dict()["images"]
    
    google_task = get_google_images(city) 
    unsplash_task = get_unsplash_images(city)
    google_imgs, unsplash_imgs = await asyncio.gather(google_task, unsplash_task) 
    
    images = google_imgs + unsplash_imgs       
    
    doc_ref.set({
        "city": city,
        "lat": lat,
        "lng": lng,
        "images": images
    })
    
    return images

# attach images
async def attach_images(places):
    
    async def process_images(place):
        images = await get_place_images(place["city"], place["lat"], place["lng"])
        place["images"] = images
        
        return place
        
    tasks = [process_images(place) for place in places]
    
    return await asyncio.gather(*tasks)