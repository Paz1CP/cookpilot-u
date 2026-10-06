const { onRequest } = require("firebase-functions/v2/https");
const { initializeApp } = require("firebase-admin/app");
const { getAuth } = require("firebase-admin/auth");
const { FieldValue, getFirestore } = require("firebase-admin/firestore");

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

function planEntryToJson(snapshot) {
  const data = snapshot.data() || {};
  return {
    id: snapshot.id,
    date: data.date || "",
    mealMoment: data.mealMoment || "",
    recipeId: data.recipeId || "",
    servings: Number(data.servings || 1),
    createdAt: timestampToIso(data.createdAt),
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

function validMealMoment(value) {
  return value === "breakfast" || value === "lunch" || value === "dinner";
}

function validatePlanEntry(body) {
  return Boolean(
    body &&
    typeof body.id === "string" &&
    body.id.trim() &&
    typeof body.date === "string" &&
    /^\d{4}-\d{2}-\d{2}$/.test(body.date) &&
    validMealMoment(body.mealMoment) &&
    typeof body.recipeId === "string" &&
    body.recipeId.trim() &&
    Number.isFinite(Number(body.servings)) &&
    Number(body.servings) >= 1
  );
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

    try {
      const path = (req.path || "/").replace(/\/+$/, "") || "/";

      if (path === "/recipes") {
        if (req.method !== "GET") {
          res.set("Allow", "GET");
          res.status(405).json({ error: "Método no permitido." });
          return;
        }

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

      const recipeMatch = path.match(/^\/recipes\/([^/]+)$/);
      if (recipeMatch) {
        if (req.method !== "GET") {
          res.set("Allow", "GET");
          res.status(405).json({ error: "Método no permitido." });
          return;
        }

        const recipeId = decodeURIComponent(recipeMatch[1]);
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

      const planCollection = db
        .collection("users")
        .doc(user.uid)
        .collection("planEntries");

      if (path === "/plan-entries") {
        if (req.method === "GET") {
          const from = String(req.query.from || "");
          const to = String(req.query.to || "");

          let query = planCollection;
          if (from) {
            query = query.where("date", ">=", from);
          }
          if (to) {
            query = query.where("date", "<=", to);
          }

          const snapshot = await query.get();
          const entries = snapshot.docs
            .map(planEntryToJson)
            .sort((a, b) => {
              const dateCompare = a.date.localeCompare(b.date);
              if (dateCompare !== 0) return dateCompare;
              return String(a.createdAt || "").localeCompare(
                String(b.createdAt || "")
              );
            });

          res.status(200).json(entries);
          return;
        }

        if (req.method === "POST") {
          if (!validatePlanEntry(req.body)) {
            res.status(400).json({ error: "Planificación inválida." });
            return;
          }

          const body = req.body;
          const reference = planCollection.doc(body.id.trim());
          await reference.set({
            date: body.date,
            mealMoment: body.mealMoment,
            recipeId: body.recipeId.trim(),
            servings: Math.max(1, Math.round(Number(body.servings))),
            createdAt: FieldValue.serverTimestamp(),
            updatedAt: FieldValue.serverTimestamp(),
          });

          const created = await reference.get();
          res.status(201).json(planEntryToJson(created));
          return;
        }

        res.set("Allow", "GET, POST");
        res.status(405).json({ error: "Método no permitido." });
        return;
      }

      const planMatch = path.match(/^\/plan-entries\/([^/]+)$/);
      if (planMatch) {
        const entryId = decodeURIComponent(planMatch[1]);
        const reference = planCollection.doc(entryId);

        if (req.method === "PUT") {
          const body = {
            ...req.body,
            id: entryId,
          };

          if (!validatePlanEntry(body)) {
            res.status(400).json({ error: "Planificación inválida." });
            return;
          }

          await reference.set({
            date: body.date,
            mealMoment: body.mealMoment,
            recipeId: body.recipeId.trim(),
            servings: Math.max(1, Math.round(Number(body.servings))),
            updatedAt: FieldValue.serverTimestamp(),
          }, { merge: true });

          const updated = await reference.get();
          res.status(200).json(planEntryToJson(updated));
          return;
        }

        if (req.method === "DELETE") {
          await reference.delete();
          res.status(204).send();
          return;
        }

        res.set("Allow", "PUT, DELETE");
        res.status(405).json({ error: "Método no permitido." });
        return;
      }

      res.status(404).json({ error: "Ruta no encontrada." });
    } catch (error) {
      console.error("Error en API", error);
      res.status(500).json({ error: "No se pudo completar la operación." });
    }
  }
);
