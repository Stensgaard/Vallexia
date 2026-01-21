<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Offer exclusion rules</h1>
        <p class="text-gray-600 text-sm">
          Configure rules to automatically exclude low-value offers (coffee/tea, soda, alcohol, non-food items) during scraping.
        </p>
      </div>
      <div class="flex items-center gap-2">
        <RouterLink class="btn btn-secondary" to="/admin/stores">Stores</RouterLink>
        <RouterLink class="btn btn-secondary" to="/admin/ingredients">Ingredient curation</RouterLink>
      </div>
    </div>

    <div v-if="error" class="bg-red-50 border border-red-200 text-red-800 p-3 rounded">
      {{ error }}
    </div>
    <div v-if="info" class="bg-blue-50 border border-blue-200 text-blue-900 p-3 rounded text-sm">
      {{ info }}
    </div>

    <!-- Rules table -->
    <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-4 space-y-3">
      <div class="flex items-center justify-between">
        <h2 class="text-lg font-semibold text-gray-900">Rules</h2>
        <div class="flex items-center gap-2">
          <button class="btn btn-secondary" @click="loadRules" :disabled="isLoading">
            {{ isLoading ? 'Loading...' : 'Refresh' }}
          </button>
          <button class="btn btn-primary" @click="openCreateModal">Create rule</button>
        </div>
      </div>

      <div class="overflow-auto border border-gray-100 rounded">
        <table class="min-w-full text-sm">
          <thead class="bg-gray-50 text-gray-700 sticky top-0">
            <tr>
              <th class="text-left p-2">Name</th>
              <th class="text-left p-2">Scope</th>
              <th class="text-left p-2">Match</th>
              <th class="text-left p-2">Patterns</th>
              <th class="text-right p-2">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="r in rules"
              :key="r.id"
              class="border-t"
              :class="!r.enabled ? 'opacity-60' : ''"
            >
              <td class="p-2">
                <div class="font-medium text-gray-900">{{ r.name }}</div>
                <div class="text-xs text-gray-500">
                  {{ r.enabled ? 'Enabled' : 'Disabled' }}
                </div>
              </td>
              <td class="p-2">
                <div class="text-xs">
                  <span class="font-medium">{{ r.scope }}</span>
                  <span v-if="r.storeName" class="text-gray-500"> · {{ r.storeName }}</span>
                </div>
              </td>
              <td class="p-2">
                <div class="text-xs text-gray-700">{{ r.matchType }}</div>
              </td>
              <td class="p-2">
                <div class="text-xs text-gray-700 max-w-xs truncate">
                  {{ r.patterns?.join(', ') || '-' }}
                </div>
              </td>
              <td class="p-2 text-right whitespace-nowrap">
                <button
                  class="btn btn-secondary text-xs mr-1"
                  @click="editRule(r)"
                  :disabled="isSaving"
                >
                  Edit
                </button>
                <button
                  v-if="r.enabled"
                  class="btn btn-secondary text-xs mr-1"
                  @click="toggleEnabled(r)"
                  :disabled="isSaving"
                >
                  Disable
                </button>
                <button
                  v-else
                  class="btn btn-secondary text-xs mr-1"
                  @click="toggleEnabled(r)"
                  :disabled="isSaving"
                >
                  Enable
                </button>
                <button
                  class="btn btn-secondary text-xs"
                  @click="deleteRule(r)"
                  :disabled="isSaving"
                >
                  Delete
                </button>
              </td>
            </tr>
            <tr v-if="rules.length === 0">
              <td colspan="5" class="p-3 text-gray-500">No rules configured.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modal overlay -->
    <div
      v-if="showModal"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50"
      @click.self="closeModal"
    >
      <div class="bg-white rounded-lg shadow-lg p-6 w-full max-w-2xl max-h-[90vh] overflow-y-auto">
        <div class="flex items-center justify-between mb-4">
          <h2 class="text-xl font-semibold text-gray-900">
            {{ editingRule ? 'Edit rule' : 'Create rule' }}
          </h2>
          <button class="text-gray-400 hover:text-gray-600" @click="closeModal">✕</button>
        </div>

        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Rule name</label>
            <input
              v-model="form.name"
              class="input w-full"
              placeholder="Rule name (e.g., Exclude coffee/tea)"
            />
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Scope</label>
              <select v-model="form.scope" class="input w-full">
                <option value="GLOBAL">Global (all stores)</option>
                <option value="STORE">Store-specific</option>
              </select>
            </div>
            <div v-if="form.scope === 'STORE'">
              <label class="block text-sm font-medium text-gray-700 mb-1">Store name</label>
              <input
                v-model="form.storeName"
                class="input w-full"
                placeholder="Store name (e.g., NETTO)"
              />
            </div>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Match type</label>
            <select v-model="form.matchType" class="input w-full">
              <option value="WORD">WORD (word-boundary safe)</option>
              <option value="CONTAINS">CONTAINS (substring match)</option>
              <option value="REGEX">REGEX (pattern match)</option>
            </select>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Patterns (one per line)</label>
            <textarea
              v-model="form.patternsText"
              class="input w-full"
              rows="6"
              placeholder="kaffe&#10;coffee&#10;espresso"
            ></textarea>
            <p class="text-xs text-gray-500 mt-1">
              Enter one pattern per line. For WORD match, patterns are matched with word boundaries.
            </p>
          </div>

          <label class="flex items-center gap-2 text-sm text-gray-700">
            <input type="checkbox" v-model="form.enabled" />
            Enabled
          </label>

          <div class="flex justify-end gap-2 pt-4 border-t">
            <button class="btn btn-secondary" @click="closeModal" :disabled="isSaving">
              Cancel
            </button>
            <button class="btn btn-primary" @click="saveRule" :disabled="isSaving">
              {{ isSaving ? 'Saving...' : editingRule ? 'Update' : 'Create' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { RouterLink } from 'vue-router';
import { storeOfferExclusionRuleService } from '@/services/storeOfferExclusionRuleService';

const error = ref('');
const info = ref('');
const isLoading = ref(false);
const isSaving = ref(false);

const rules = ref([]);
const editingRule = ref(null);
const showModal = ref(false);

const form = ref({
  name: '',
  enabled: true,
  scope: 'GLOBAL',
  storeName: '',
  matchType: 'WORD',
  patternsText: '',
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
  editingRule.value = null;
  form.value = {
    name: '',
    enabled: true,
    scope: 'GLOBAL',
    storeName: '',
    matchType: 'WORD',
    patternsText: '',
  };
};

const openCreateModal = () => {
  resetForm();
  showModal.value = true;
};

const closeModal = () => {
  showModal.value = false;
  resetForm();
};

const loadRules = async () => {
  error.value = '';
  info.value = '';
  isLoading.value = true;
  try {
    rules.value = await storeOfferExclusionRuleService.listRules();
  } catch (e) {
    error.value = extractErrorMessage(e, 'Failed to load rules');
  } finally {
    isLoading.value = false;
  }
};

const saveRule = async () => {
  error.value = '';
  info.value = '';
  isSaving.value = true;
  try {
    const patterns = (form.value.patternsText || '')
      .split('\n')
      .map((s) => s.trim())
      .filter(Boolean);

    if (patterns.length === 0) {
      error.value = 'At least one pattern is required';
      return;
    }

    const payload = {
      name: form.value.name,
      enabled: form.value.enabled,
      scope: form.value.scope,
      storeName: form.value.scope === 'STORE' ? form.value.storeName : null,
      matchType: form.value.matchType,
      patterns: patterns,
    };

    if (editingRule.value) {
      await storeOfferExclusionRuleService.updateRule(editingRule.value.id, payload);
      info.value = 'Rule updated';
    } else {
      await storeOfferExclusionRuleService.createRule(payload);
      info.value = 'Rule created';
    }
    await loadRules();
    closeModal();
  } catch (e) {
    error.value = extractErrorMessage(e, 'Failed to save rule');
  } finally {
    isSaving.value = false;
  }
};

const editRule = (rule) => {
  editingRule.value = rule;
  form.value = {
    name: rule.name,
    enabled: rule.enabled,
    scope: rule.scope,
    storeName: rule.storeName || '',
    matchType: rule.matchType,
    patternsText: (rule.patterns || []).join('\n'),
  };
  showModal.value = true;
};

const toggleEnabled = async (rule) => {
  error.value = '';
  info.value = '';
  isSaving.value = true;
  try {
    if (rule.enabled) {
      await storeOfferExclusionRuleService.disableRule(rule.id);
      info.value = 'Rule disabled';
    } else {
      await storeOfferExclusionRuleService.enableRule(rule.id);
      info.value = 'Rule enabled';
    }
    await loadRules();
  } catch (e) {
    error.value = extractErrorMessage(e, 'Failed to toggle rule');
  } finally {
    isSaving.value = false;
  }
};

const deleteRule = async (rule) => {
  if (!confirm(`Delete rule "${rule.name}"?`)) {
    return;
  }
  error.value = '';
  info.value = '';
  isSaving.value = true;
  try {
    await storeOfferExclusionRuleService.deleteRule(rule.id);
    info.value = 'Rule deleted';
    await loadRules();
  } catch (e) {
    error.value = extractErrorMessage(e, 'Failed to delete rule');
  } finally {
    isSaving.value = false;
  }
};

onMounted(async () => {
  await loadRules();
});
</script>
