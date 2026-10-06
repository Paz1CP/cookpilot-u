const { onRequest } = require("firebase-functions/v2/https");
const { initializeApp } = require("firebase-admin/app");
const { getAuth } = require("firebase-admin/auth");
const { FieldValue, getFirestore } = require("firebase-admin/firestore");

initializeApp();
const db = getFirestore();

const RECIPE_FINANCIALS = {
  "ae9f5a24-e906-4116-9022-d54f6e749ba5": [16.99, 23.09, 126.00],
  "14efb3a4-d607-4ca4-a22c-7775811d48d0": [29.88, 38.91, 80.00],
  "0b135dfc-beb4-4412-8688-b5e577cd65e0": [12.71, 20.00, 78.00],
  "8a5bb7cb-0eab-4afd-8f24-a6e7b8d0b33b": [19.17, 26.79, 80.00],
  "d3288868-0a80-4d50-b44a-fe472940fcab": [18.60, 27.42, 112.00],
  "4fc73bf1-dd86-434c-a60c-a26f02561a1d": [20.62, 31.28, 121.00],
  "e79429bc-f358-4292-a1e9-81f9a50dd526": [3.76, 5.71, 16.00],
  "2cbe3250-f4e9-4cbe-aff6-44e38e8f5707": [5.26, 8.30, 50.00],
  "58acec48-a0b4-4393-bd50-77b7480cd058": [7.22, 9.11, 31.80],
  "6eefbbc4-7a7c-4934-94a0-352875f95a63": [30.48, 35.15, 120.00],
  "7baad262-e7ba-4878-b882-a4ba1f0a41d3": [22.88, 29.43, 80.00],
  "9ddc1894-2523-4622-be69-604061c39e52": [6.36, 8.71, 50.00],
  "c6a61cff-3289-488b-a61c-d951e491aafc": [18.46, 26.49, 64.00],
  "579e0bd0-e1c7-44f9-80af-a162b4e5ea47": [31.11, 41.91, 70.00],
  "be3c564a-8519-4547-a01f-e397e53cb2d2": [10.88, 14.83, 50.00],
  "6ba24ac2-3a8f-48f2-8d45-1ec2f1f0720a": [14.68, 18.63, 92.00],
  "2c8d754c-9b87-462f-9507-64c7939ac0cf": [14.87, 22.11, 112.00],
  "d08e6b64-95a8-4307-8fcd-c22256c881a7": [19.26, 28.30, 92.00],
};

function financialForRecipe(recipeId) {
  const values = RECIPE_FINANCIALS[recipeId];
  if (!values) {
    return { estimatedCostPen: 0, estimatedSavingsPen: 0 };
  }

  const homeCost = (values[0] + values[1]) / 2;
  return {
    estimatedCostPen: homeCost,
    estimatedSavingsPen: Math.max(0, values[2] - homeCost),
  };
}

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
  const financial = financialForRecipe(snapshot.id);
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
    estimatedCostPen: financial.estimatedCostPen,
    estimatedSavingsPen: financial.estimatedSavingsPen,
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