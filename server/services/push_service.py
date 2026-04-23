from firebase_admin import messaging

def send_push(token, title, body):
    try:
        message = messaging.Message(
            data={    
                "title": title,
                "body": body
            },
            android=messaging.AndroidConfig(
                priority="high"
            ),
            token=token,
        )

        response = messaging.send(message)
        print("✅ PUSH SENT:", response)
    except Exception as e:
        print("❌ PUSH ERROR:", str(e))