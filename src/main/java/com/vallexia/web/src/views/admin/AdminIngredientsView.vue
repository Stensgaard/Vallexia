<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Ingredient curation</h1>
        <p class="text-gray-600 text-sm">
          Match scraped offers to canonical ingredients and build aliases over time.
        </p>
      </div>
      <div class="flex items-center gap-2">
        <button class="btn btn-secondary" @click="runScrapeStore" :disabled="!resolvedStoreId || isScrapingStore || isScrapingAll">
          {{ isScrapingStore ? "Scraping..." : "Scrape selected store" }}
        </button>
        <button class="btn btn-secondary" @click="runScrapeAll" :disabled="isScrapingStore || isScrapingAll">
          {{ isScrapingAll ? "Scraping..." : "Scrape all stores" }}
        </button>
        <button class="btn btn-primary" @click="runMatching" :disabled="isMatching">
          {{ isMatching ? "Matching..." : "Run matching now" }}
        </button>
      </div>
    </div>

    <div v-if="error" class="bg-red-50 border border-red-200 text-red-800 p-3 rounded">
      {{ error }}
    </div>

    <div
      v-if="info"
      class="bg-blue-50 border border-blue-200 text-blue-900 p-3 rounded text-sm"
    >
      {{ info }}
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <!-- Unmatched offers -->
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-4">
        <div class="flex items-center justify-between mb-4">
          <h2 class="text-lg font-semibold text-gray-900">Unmatched offers</h2>
          <button class="btn btn-secondary" @click="loadUnmatched" :disabled="isLoadingUnmatched">
            {{ isLoadingUnmatched ? "Loading..." : "Refresh" }}
          </button>
        </div>

        <div class="flex gap-2 mb-3">
          <input
            v-model="storeQuery"
            list="store-list"
            class="input flex-1"
            placeholder="Optional store (e.g. Bilka)"
          />
          <datalist id="store-list">
            <option v-for="s in stores" :key="s.id" :value="s.displayName"></option>
          </datalist>
          <button class="btn btn-secondary" @click="loadUnmatched">Load</button>
        </div>

        <div class="flex items-center justify-between mb-2">
          <div class="text-sm text-gray-600">
            {{ unmatchedOffers.length }} offers
          </div>
          <label class="flex items-center gap-2 text-sm text-gray-700">
            <input type="checkbox" v-model="showOfferDetails" />
            Show details
          </label>
          <label class="flex items-center gap-2 text-sm text-gray-700">
            <input type="checkbox" v-model="showDismissed" @change="loadUnmatched" />
            Show dismissed
          </label>
        </div>

        <div class="overflow-auto max-h-[520px] border border-gray-100 rounded">
          <table class="min-w-full text-sm">
            <thead class="bg-gray-50 text-gray-700 sticky top-0">
              <tr>
                <th class="text-left p-2">Store</th>
                <th class="text-left p-2">Product</th>
                <th class="text-right p-2">Price</th>
                <th class="text-right p-2">Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="o in unmatchedOffers"
                :key="o.offerId"
                class="border-t cursor-pointer"
                :class="selectedOffer?.offerId === o.offerId ? 'bg-blue-50' : 'hover:bg-gray-50'"
                @click="selectOffer(o)"
              >
                <td class="p-2 whitespace-nowrap">{{ o.storeName }}</td>
                <td class="p-2">
                  <div class="font-medium text-gray-900">{{ o.displayProductName || o.productName }}</div>
                  <div v-if="formatHowMany(o)" class="text-xs text-gray-500">
                    {{ formatHowMany(o) }}
                  </div>
                  <div v-if="showOfferDetails && o.normalizedProductName && o.normalizedProductName !== o.productName" class="text-xs text-gray-500">
                    Normalized: {{ o.normalizedProductName }}
                  </div>
                  <div v-if="showOfferDetails && o.rawPriceText" class="text-xs text-gray-500">
                    Details: {{ o.rawPriceText }}
                  </div>
                  <div class="text-xs text-gray-500">
                    OfferId: {{ o.offerId }} · {{ o.validFrom }} → {{ o.validTo }}
                  </div>
                </td>
                <td class="p-2 text-right whitespace-nowrap">
                  <div>{{ o.price ?? "-" }}</div>
                </td>
                <td class="p-2 text-right whitespace-nowrap">
                  <button
                    v-if="!o.dismissed"
                    class="btn btn-secondary text-xs"
                    @click.stop="dismissOffer(o)"
                    :disabled="isDismissing"
                  >
                    Dismiss
                  </button>
                  <button
                    v-else
                    class="btn btn-secondary text-xs"
                    @click.stop="undoDismissOffer(o)"
                    :disabled="isDismissing"
                  >
                    Undo
                  </button>
                </td>
              </tr>
              <tr v-if="unmatchedOffers.length === 0">
                <td colspan="4" class="p-3 text-gray-500">No unmatched offers.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Ingredient search + create + match -->
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-4 space-y-4">
        <h2 class="text-lg font-semibold text-gray-900">Ingredient picker</h2>

        <div class="p-3 border rounded bg-gray-50">
          <div class="text-sm text-gray-700">
            <span class="font-semibold">Selected offer:</span>
            <span v-if="selectedOffer" class="ml-1">{{ selectedOffer.productName }}</span>
            <span v-else class="text-gray-500 ml-1">Pick one from the left</span>
          </div>
        </div>

        <div class="flex gap-2 items-center">
          <input
            v-model="searchQuery"
            class="input flex-1"
            placeholder="Search ingredient (English)..."
          />
          <span v-if="isSearching" class="text-sm text-gray-500">Searching...</span>
        </div>

        <div class="border rounded overflow-auto max-h-[240px]">
          <table class="min-w-full text-sm">
            <thead class="bg-gray-50 text-gray-700 sticky top-0">
              <tr>
                <th class="text-left p-2">Name</th>
                <th class="text-left p-2">Canonical</th>
                <th class="text-left p-2">Id</th>
                <th class="text-right p-2">Action</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="r in searchResults" :key="r.ingredientId" class="border-t">
                <td class="p-2">{{ r.localizedName }}</td>
                <td class="p-2 text-gray-600">{{ r.canonicalName }}</td>
                <td class="p-2 text-gray-600">{{ r.ingredientId }}</td>
                <td class="p-2 text-right">
                  <button
                    class="btn btn-primary"
                    :disabled="!selectedOffer || isApplyingMatch"
                    @click="applyMatch(r)"
                  >
                    Match
                  </button>
                </td>
              </tr>
              <tr v-if="searchResults.length === 0">
                <td colspan="4" class="p-3 text-gray-500">No results.</td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="border-t pt-4">
          <h3 class="text-md font-semibold text-gray-900 mb-2">Create ingredient</h3>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-2">
            <input
              v-model="createCanonical"
              class="input"
              placeholder="English (canonical, e.g. butter)"
            />
            <input v-model="createDa" class="input" placeholder="Danish (e.g. smør)" />
          </div>
          <div class="flex justify-end mt-2">
            <button class="btn btn-secondary" @click="createIngredient" :disabled="isCreating">
              {{ isCreating ? "Creating..." : "Create" }}
            </button>
          </div>
          <p class="text-xs text-gray-500 mt-1">
            Tip: after creating, search again and match the selected offer. You can also check
            “create alias” by default (we do).
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from "vue";
import { adminIngredientService } from "@/services/adminIngredientService";
import { storeOffersAdminService } from "@/services/storeOffersAdminService";

const error = ref("");

const extractErrorMessage = (e, fallback) => {
  // Prefer backend-provided specific messages in ErrorResponseDto.details.
  const details = e?.response?.data?.details;
  if (details && typeof details === "object") {
    if (details.argument) return details.argument;
    if (details.error) return details.error;
    const first = Object.values(details).find((v) => typeof v === "string" && v.trim());
    if (first) return first;
  }
  return e?.response?.data?.message || e?.message || fallback;
};

const stores = ref([]);
const storeQuery = ref("");
const unmatchedOffers = ref([]);
const selectedOffer = ref(null);
const isLoadingUnmatched = ref(false);
const isScrapingStore = ref(false);
const isScrapingAll = ref(false);
const showDismissed = ref(false);
const showOfferDetails = ref(false);
const isDismissing = ref(false);

const searchQuery = ref("");
const searchResults = ref([]);
const isSearching = ref(false);
const isApplyingMatch = ref(false);

const createCanonical = ref("");
const createDa = ref("");
const isCreating = ref(false);

const isMatching = ref(false);
const info = ref("");

const formatHowMany = (o) => {
  const name = (o?.productName || "").toLowerCase();
  const displayName = (o?.displayProductName || "").toLowerCase();
  const isEgg = name.includes("æg") || name.includes("egg") || displayName.includes("æg") || displayName.includes("egg");

  // Prefer package size/count if present.
  if (o?.packageQtyMin) {
    const unit = (o?.packageUnit || "").toLowerCase();
    const qtyMin = o.packageQtyMin;
    const qtyMax = o.packageQtyMax;

    if (unit === "stk") {
      return isEgg ? `${qtyMin} eggs` : `${qtyMin} stk`;
    }

    if (qtyMax) {
      return unit ? `${qtyMin}–${qtyMax} ${unit}` : `${qtyMin}–${qtyMax}`;
    }

    return unit ? `${qtyMin} ${unit}` : `${qtyMin}`;
  }

  // Fallback: if we only have a min-purchase (true multi-buy), show it.
  if (o?.minPurchaseQty) {
    return `${o.minPurchaseQty} ${o.minPurchaseUnit || "stk"}`;
  }

  // Fallback: some offers (especially meat/counter items) are priced per kg with no fixed pack size.
  const raw = (o?.rawPriceText || "").replace(/\s+/g, " ").trim();
  if (raw) {
    // Examples: "Pr. kg 97,50." / "Pr. kg max. 78,13."
    const m = raw.match(/pr\.?\s*kg\s*(?:max\.\s*)?(\d{1,4}(?:[.,]\d{1,2})?)/i);
    if (m && m[1]) {
      return `per kg ${m[1]}`;
    }
  }

  return "";
};

const selectOffer = (offer) => {
  selectedOffer.value = offer;
};

const resolveStoreId = () => {
  const q = storeQuery.value?.trim();
  if (!q) return null;
  const match = stores.value.find(
    (s) => s.displayName?.toLowerCase() === q.toLowerCase() || s.name?.toLowerCase() === q.toLowerCase(),
  );
  return match ? match.id : null;
};

const resolvedStoreId = computed(() => resolveStoreId());

const runScrapeStore = async () => {
  const storeId = resolveStoreId();
  if (!storeId) return;
  error.value = "";
  info.value = "";
  isScrapingStore.value = true;
  try {
    const resp = await storeOffersAdminService.triggerScrapeStore(storeId);
    info.value = `Scrape completed for selected store. offersCount=${resp?.offersCount ?? "?"}`;
    await loadUnmatched();
  } catch (e) {
    error.value = extractErrorMessage(e, "Failed to scrape store");
  } finally {
    isScrapingStore.value = false;
  }
};

const runScrapeAll = async () => {
  const ok = window.confirm("Scrape all stores now? This may take a while.");
  if (!ok) return;
  error.value = "";
  info.value = "";
  isScrapingAll.value = true;
  try {
    const resp = await storeOffersAdminService.triggerScrapeAllStores();
    info.value = `Scrape completed for all stores. totalOffers=${resp?.totalOffers ?? "?"}`;
    await loadUnmatched();
  } catch (e) {
    error.value = extractErrorMessage(e, "Failed to scrape all stores");
  } finally {
    isScrapingAll.value = false;
  }
};

const loadUnmatched = async () => {
  error.value = "";
  isLoadingUnmatched.value = true;
  try {
    const storeId = resolveStoreId();
    unmatchedOffers.value = await storeOffersAdminService.getUnmatchedOffers({
      storeId,
      includeDismissed: showDismissed.value,
    });

    if (selectedOffer.value) {
      const stillExists = unmatchedOffers.value.some(
        (o) => o.offerId === selectedOffer.value.offerId,
      );
      if (!stillExists) {
        selectedOffer.value = null;
      }
    }
  } catch (e) {
    error.value = extractErrorMessage(e, "Failed to load unmatched offers");
  } finally {
    isLoadingUnmatched.value = false;
  }
};

const dismissOffer = async (offer) => {
  error.value = "";
  isDismissing.value = true;
  try {
    await storeOffersAdminService.dismissOffer(offer.offerId);
    await loadUnmatched();
  } catch (e) {
    error.value = extractErrorMessage(e, "Failed to dismiss offer");
  } finally {
    isDismissing.value = false;
  }
};

const undoDismissOffer = async (offer) => {
  error.value = "";
  isDismissing.value = true;
  try {
    await storeOffersAdminService.undoDismissOffer(offer.offerId);
    await loadUnmatched();
  } catch (e) {
    error.value = extractErrorMessage(e, "Failed to undo dismiss");
  } finally {
    isDismissing.value = false;
  }
};

const runMatching = async () => {
  error.value = "";
  info.value = "";
  isMatching.value = true;
  try {
    const resp = await storeOffersAdminService.triggerMatching();
    info.value = `Matching completed. matchedCount=${resp?.matchedCount ?? "?"}, unmatchedRemaining=${resp?.unmatchedRemaining ?? "?"}`;
    await loadUnmatched();
  } catch (e) {
    error.value = extractErrorMessage(e, "Failed to run matching");
  } finally {
    isMatching.value = false;
  }
};

const searchIngredients = async () => {
  error.value = "";
  isSearching.value = true;
  try {
    searchResults.value = await adminIngredientService.searchIngredients({
      q: searchQuery.value,
      locale: "en",
      limit: 25,
    });
  } catch (e) {
    error.value = extractErrorMessage(e, "Ingredient search failed");
  } finally {
    isSearching.value = false;
  }
};

let searchDebounceTimer = null;
watch(searchQuery, (q) => {
  if (searchDebounceTimer) {
    clearTimeout(searchDebounceTimer);
  }
  const query = (q || "").trim();
  if (!query) {
    searchResults.value = [];
    return;
  }
  // Debounce typing to avoid spamming the API.
  searchDebounceTimer = setTimeout(() => {
    // Avoid calling search for single characters.
    if (query.length < 2) {
      searchResults.value = [];
      return;
    }
    searchIngredients();
  }, 250);
});

const applyMatch = async (ingredient) => {
  if (!selectedOffer.value) return;
  error.value = "";
  isApplyingMatch.value = true;
  try {
    await storeOffersAdminService.manualMatchOffer({
      offerId: selectedOffer.value.offerId,
      ingredientId: ingredient.ingredientId,
      // Alias should be in the offer locale (current flyers are Danish).
      locale: "da",
      createAlias: true,
    });
    await loadUnmatched();
  } catch (e) {
    error.value = extractErrorMessage(e, "Failed to apply match");
  } finally {
    isApplyingMatch.value = false;
  }
};

const createIngredient = async () => {
  error.value = "";
  isCreating.value = true;
  try {
    const translations = {};
    if (createDa.value) translations.da = createDa.value;
    // Canonical is always English; keep en translation aligned with canonical.
    if (createCanonical.value) translations.en = createCanonical.value;

    await adminIngredientService.createIngredient({
      canonicalName: createCanonical.value,
      translations,
    });
    // Refresh search list
    if (createCanonical.value) {
      searchQuery.value = createCanonical.value;
    }
    await searchIngredients();
  } catch (e) {
    error.value = extractErrorMessage(e, "Failed to create ingredient");
  } finally {
    isCreating.value = false;
  }
};

onMounted(async () => {
  try {
    stores.value = await storeOffersAdminService.getStores();
  } catch (e) {
    // ignore; store filter becomes optional only
  }
  await loadUnmatched();
});
</script>

