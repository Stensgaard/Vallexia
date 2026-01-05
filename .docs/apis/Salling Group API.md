🛒 Getting Discounted Food Items from Bilka API
1. Identify the Local Store
Find the nearest Bilka store to the user (based on city/postal code).
Retrieve the storeId for that Bilka location (needed for API calls).

2. Query Offers
Call: http
GET https://api.sallinggroup.com/v1/offers?storeId=<bilka_store_id>&expand=product
Authorization: Bearer YOUR_API_KEY
This returns all discounted items (food + non-food) for that store.
The expand=product ensures product details are included in the same response.

3. Filter Food Items
Use the product.category or product.department field.
Keep only categories like:
 - Fruit & Vegetables
 - Meat & Fish
 - Dairy
 - Bakery
 - Pantry
 - Drinks

Discard non-food categories (Electronics, Textiles, Home & Garden, etc.).
figure out which categories they use and include only the relevant ones while discarding the non food ones

4. Process & Display
Extract relevant fields:
 - Product name
 - Original price
 - Discounted price
 - Discount percentage
 - Validity period

5. Optimize
Cache product metadata (names, categories) since they rarely change.
Refresh offers daily/weekly to stay in sync with Bilka’s “tilbudsavis.”
Handle rate limits by avoiding per-item product calls — rely on expand=product.

⚙️ Example Flow
User in Aalborg → nearest Bilka store ID = bilka123.
Call /offers?storeId=bilka123&expand=product.
Response includes apples, milk, bread, plus non-food like speakers.
Filter categories → keep apples, milk, bread.
Show list of discounted food items with prices and discount percentages.

✅ Summary
Step 1: Find local Bilka store ID.
Step 2: Query /offers with expand=product.
Step 3: Filter by food categories.
Step 4: Display clean list of discounted food items.
Step 5: Optimize with caching and scheduled refreshes.
