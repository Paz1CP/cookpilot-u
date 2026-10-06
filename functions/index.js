const { onRequest } = require("firebase-functions/v2/https");
const { initializeApp } = require("firebase-admin/app");
const { getAuth } = require("firebase-admin/auth");
const { getFirestore } = require("firebase-admin/firestore");

initializeApp();
const db = getFirestore();

async function requireUser(req, res) {
  const authorization = req.get("Authorization") || "";
  const match = authorization.match(/^Bearer\s+(.+)$/i);

  if (!match) {
    res.status(401).json({ error: "Falta el token de autenticación." });
    return null;
  }

  try {
    return await getAuth().verifyIdToken(match[1]);
  } catch (error) {
    res.status(401).json({ error: "La sesión no es válida." });
    return null;
  }
}

function timestampToIso(value) {
  if (!value || typeof value.toDate !== "function") {
    return null;
  }
  return value.toDate().toISOString();
}

function recipeToJson(snapshot, ingredientMap) {
  const data = snapshot.data() || {};
  const compositions = Array.isArray(data.ingredients)
    ? data.ingredients
    : [];

  const ingredients = compositions.map((composition) => {
    const identity = ingredientMap.get(composition.ingredientId) || {};

    return {
      ingredientId: composition.ingredientId,
      name: identity.name || "",
      imageUrl: identity.imageUrl || null,
      quantity: Number(composition.quantity || 0),
      unit: composition.unit || "",
      optional: Boolean(composition.optional),
    };
  });

  return {
    id: snapshot.id,
    canonicalName: data.canonicalName || "",
    title: data.title || "",
    description: data.description || "",
    imageUrl: data.imageUrl || null,
    servings: Number(data.servings || 1),
    difficulty: data.difficulty || "medium",
    totalMinutes: Number(data.totalMinutes || 0),
    activeMinutes: Number(data.activeMinutes || 0),
    passiveMinutes: Number(data.passiveMinutes || 0),
    categorySlugs: Array.isArray(data.categorySlugs)
      ? data.categorySlugs
      : [],
    nutrition: data.nutrition || {
      calories: 0,
      proteinG: 0,
      carbsG: 0,
      fatG: 0,
      fiberG: 0,
    },
    ingredients,
    steps: Array.isArray(data.steps) ? data.steps : [],
    updatedAt: timestampToIso(data.updatedAt),
  };
}

async function loadIngredientMap() {
  const snapshot = await db.collection("ingredients").get();
  const result = new Map();

  snapshot.docs.forEach((document) => {
    result.set(document.id, document.data() || {});
  });

  return result;
}

exports.api = onRequest(
  {
    region: "southamerica-west1",
    cors: false,
  },
  async (req, res) => {
    const user = await requireUser(req, res);
    if (!user) {
      return;
    }

    if (req.method !== "GET") {
      res.set("Allow", "GET");
      res.status(405).json({ error: "Método no permitido." });
      return;
    }

    try {
      const path = (req.path || "/").replace(/\/+$/, "") || "/";

      if (path === "/recipes") {
        const [recipesSnapshot, ingredientMap] = await Promise.all([
          db.collection("recipes").orderBy("title").get(),
          loadIngredientMap(),
        ]);

        const recipes = recipesSnapshot.docs.map((document) =>
          recipeToJson(document, ingredientMap)
        );

        res.status(200).json(recipes);
        return;
      }

      const match = path.match(/^\/recipes\/([^/]+)$/);
      if (match) {
        const recipeId = decodeURIComponent(match[1]);
        const [recipeSnapshot, ingredientMap] = await Promise.all([
          db.collection("recipes").doc(recipeId).get(),
          loadIngredientMap(),
        ]);

        if (!recipeSnapshot.exists) {
          res.status(404).json({ error: "Receta no encontrada." });
          return;
        }

        res.status(200).json(
          recipeToJson(recipeSnapshot, ingredientMap)
        );
        return;
      }

      res.status(404).json({ error: "Ruta no encontrada." });
    } catch (error) {
      console.error("Error al consultar recetas", error);
      res.status(500).json({ error: "No se pudo cargar el catálogo." });
    }
  }
);
