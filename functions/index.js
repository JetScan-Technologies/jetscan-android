const { onRequest } = require("firebase-functions/v2/https");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");
const { getMessaging } = require("firebase-admin/messaging");

initializeApp();

exports.getUser = onRequest(
    { cors: true },
    async (req, res) => {
        try {
            if (req.method !== "GET") {
                res.status(405).send({ message: "Method not allowed" });
            }
            if (!req.query.userId) {
                res.status(400).send({ message: "User id is required" });
            }
            const userId = req.query.userId;
            const userRef = getFirestore().collection("users").doc(userId);
            const userDoc = await userRef.get();
            if (!userDoc.exists) {
                res.status(404).send({ message: "User not found" });
            }
            res.status(200).send({ user: userDoc.data() });
        } catch (error) {
            res.status(500).send(
                JSON.stringify({
                    message: error.message,
                    code: error.code,
                    error: error,
                })
            )
        }
    }
);

exports.testNotification = onRequest(
    { cors: true },
    async (req, res) => {
        try {
            if (req.method !== "POST") {
                res.status(405).send({ message: "Method not allowed" });
            }
            if (!req.body.userId) {
                res.status(400).send({ message: "User id is required" });
            }
            const userId = req.body.userId;
            const userRef = getFirestore().collection("users").doc(userId);
            const userDoc = await userRef.get();
            if (!userDoc.exists) {
                res.status(404).send({ message: "User not found" });
            }
            const user = userDoc.data();
            const message = {
                token: user.token,
                notification: {
                    title: req.body.title || "Test Notification",
                    body: req.body.body || "This is a test notification",
                },
            };
            const response = await getMessaging().send(message);
            res.status(200).send({ response: response });
        } catch (error) {
            res.status(500).send(
                JSON.stringify({
                    message: error.message,
                    code: error.code,
                    error: error,
                })
            )
        }
    }
);