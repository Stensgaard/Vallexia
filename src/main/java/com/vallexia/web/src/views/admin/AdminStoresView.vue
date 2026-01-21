<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Store management</h1>
        <p class="text-gray-600 text-sm">Add and manage store flyer sources.</p>
      </div>
      <div class="flex items-center gap-2">
        <RouterLink class="btn btn-secondary" to="/admin/ingredients">Ingredient curation</RouterLink>
        <RouterLink class="btn btn-secondary" to="/admin/offer-filters">Offer filters</RouterLink>
      </div>
    </div>

    <div v-if="error" class="bg-red-50 border border-red-200 text-red-800 p-3 rounded">
      {{ error }}
    </div>
    <div v-if="info" class="bg-blue-50 border border-blue-200 text-blue-900 p-3 rounded text-sm">
      {{ info }}
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <!-- Create store -->
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-4 space-y-3">
        <div class="flex items-center justify-between">
          <h2 class="text-lg font-semibold text-gray-900">Add store</h2>
          <button class="btn btn-secondary" @click="resetForm" :disabled="isSaving">Reset</button>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-2">
          <input v-model="form.name" class="input" placeholder="Name (unique key, e.g. BILKA)" />
          <input v-model="form.displayName" class="input" placeholder="Display name (e.g. Bilka)" />
        </div>
        <input v-model="form.flyerUrl" class="input" placeholder="Flyer URL" />
        <input v-model="form.websiteUrl" class="input" placeholder="Website URL (optional)" />

        <input
          v-model="form.foodFlyerKeywordsText"
          class="input"
          placeholder="Food flyer keywords (comma-separated, optional)"
        />

        <div class="grid grid-cols-1 md:grid-cols-3 gap-2">
          <label class="flex items-center gap-2 text-sm text-gray-700">
            <input type="checkbox" v-model="form.scrapeEnabled" />
            scrape enabled
          </label>
          <input v-model="form.scrapeCron" class="input" placeholder="Scrape cron" />
          <input v-model="form.scrapeZone" class="input" placeholder="Scrape zone" />
        </div>

        <div class="flex justify-end">
          <button class="btn btn-primary" @click="createStore" :disabled="isSaving">
            {{ isSaving ? "Saving..." : "Create store" }}
          </button>
        </div>
      </div>

      <!-- Store list -->
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-4 space-y-3">
        <div class="flex items-center justify-between">
          <h2 class="text-lg font-semibold text-gray-900">Stores</h2>
          <button class="btn btn-secondary" @click="loadStores" :disabled="isLoading">
            {{ isLoading ? "Loading..." : "Refresh" }}
          </button>
        </div>

        <div class="overflow-auto max-h-[520px] border border-gray-100 rounded">
          <table class="min-w-full text-sm">
            <thead class="bg-gray-50 text-gray-700 sticky top-0">
              <tr>
                <th class="text-left p-2">Name</th>
                <th class="text-left p-2">Flyer URL</th>
                <th class="text-left p-2">Status</th>
                <th class="text-right p-2">Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="s in stores" :key="s.id" class="border-t">
                <td class="p-2">
                  <div class="font-medium text-gray-900">{{ s.displayName }}</div>
                  <div class="text-xs text-gray-500">{{ s.name }} · id {{ s.id }}</div>
                </td>
                <td class="p-2">
                  <div class="text-xs break-all text-gray-700">{{ s.flyerUrl }}</div>
                  <div v-if="s.websiteUrl" class="text-xs break-all text-gray-500">
                    {{ s.websiteUrl }}
                  </div>
                </td>
                <td class="p-2">
                  <div class="text-xs">
                    <span
                      :class="s.scrapeEnabled ? 'text-green-700' : 'text-gray-500'"
                      class="font-medium"
                    >
                      {{ s.scrapeEnabled ? "enabled" : "disabled" }}
                    </span>
                  </div>
                  <div v-if="s.lastScrapedAt" class="text-xs text-gray-500">
                    last: {{ s.lastScrapedAt }}
                  </div>
                  <div v-if="s.nextScrapeAt" class="text-xs text-gray-500">
                    next: {{ s.nextScrapeAt }}
                  </div>
                  <div v-if="s.consecutiveFailures" class="text-xs text-red-700">
                    failures: {{ s.consecutiveFailures }}
                  </div>
                </td>
                <td class="p-2 text-right whitespace-nowrap">
                  <button class="btn btn-secondary mr-2" @click="scrapeNow(s)" :disabled="isScraping">
                    Scrape now
                  </button>
                  <button class="btn btn-secondary" @click="toggleEnabled(s)" :disabled="isSaving">
                    {{ s.scrapeEnabled ? "Disable" : "Enable" }}
                  </button>
                </td>
              </tr>
              <tr v-if="stores.length === 0">
                <td colspan="4" class="p-3 text-gray-500">No stores.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { RouterLink } from 'vue-router';
import { adminStoreService } from '@/services/adminStoreService';
import { storeOffersAdminService } from '@/services/storeOffersAdminService';

const error = ref('');
const info = ref('');
const isLoading = ref(false);
const isSaving = ref(false);
const isScraping = ref(false);

const stores = ref([]);

const form = ref({
  name: '',
  displayName: '',
  flyerUrl: '',
  websiteUrl: '',
  foodFlyerKeywordsText: '',
  scrapeEnabled: true,
  scrapeCron: '0 0 2 * * MON',
  scrapeZone: 'Europe/Copenhagen',
});

const extractErrorMessage = (e, fallback) => {
  const details = e?.response?.data?.details;
  if (details && typeof details === 'object') {
    if (details.argument) return details.argument;
    if (details.error) return details.error;
    const first = Object.values(details).find((v) => typeof v === 'string' && v.trim());
    if (first) return first;
  }
  return e?.response?.data?.message || e?.message || fallback;
};

const resetForm = () => {
  form.value = {
    name: '',
    displayName: '',
    flyerUrl: '',
    websiteUrl: '',
    foodFlyerKeywordsText: '',
    scrapeEnabled: true,
    scrapeCron: '0 0 2 * * MON',
    scrapeZone: 'Europe/Copenhagen',
  };
};

const loadStores = async () => {
  error.value = '';
  info.value = '';
  isLoading.value = true;
  try {
    stores.value = await adminStoreService.listStores();
  } catch (e) {
    error.value = extractErrorMessage(e, 'Failed to load stores');
  } finally {
    isLoading.value = false;
  }
};

const createStore = async () => {
  error.value = '';
  info.value = '';
  isSaving.value = true;
  try {
    const keywords = (form.value.foodFlyerKeywordsText || '')
      .split(',')
      .map((s) => s.trim())
      .filter(Boolean);

    await adminStoreService.createStore({
      name: form.value.name,
      displayName: form.value.displayName,
      flyerUrl: form.value.flyerUrl,
      websiteUrl: form.value.websiteUrl || null,
      foodFlyerKeywords: keywords,
      scrapeEnabled: !!form.value.scrapeEnabled,
      scrapeCron: form.value.scrapeCron,
      scrapeZone: form.value.scrapeZone,
    });
    info.value = 'Store created';
    await loadStores();
    resetForm();
  } catch (e) {
    error.value = extractErrorMessage(e, 'Failed to create store');
  } finally {
    isSaving.value = false;
  }
};

const toggleEnabled = async (store) => {
  error.value = '';
  info.value = '';
  isSaving.value = true;
  try {
    await adminStoreService.updateStore(store.id, { scrapeEnabled: !store.scrapeEnabled });
    await loadStores();
  } catch (e) {
    error.value = extractErrorMessage(e, 'Failed to update store');
  } finally {
    isSaving.value = false;
  }
};

const scrapeNow = async (store) => {
  error.value = '';
  info.value = '';
  isScraping.value = true;
  try {
    await storeOffersAdminService.triggerScrapeStore(store.id);
    info.value = `Scrape triggered for ${store.displayName}`;
    await loadStores();
  } catch (e) {
    error.value = extractErrorMessage(e, 'Failed to scrape store');
  } finally {
    isScraping.value = false;
  }
};

onMounted(async () => {
  await loadStores();
});
</script>

