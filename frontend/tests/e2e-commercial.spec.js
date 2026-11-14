import { test, expect } from '@playwright/test';

const BASE_URL = 'http://localhost:5173';

test.describe('Tests E2E - Gestion Commerciale', () => {
  test('1. Refus de connexion avec mauvais mot de passe', async ({ page }) => {
    await page.goto(BASE_URL);
    await page.getByTestId('email-input').fill('jihad@admin');
    await page.getByTestId('password-input').fill('mauvais_mdp');
    await page.getByTestId('login-btn').click();
    await expect(page.getByTestId('error-msg')).toBeVisible();
  });

  test('2. Parcours complet : Login -> Produit -> Commande -> Stock -> Confirmation', async ({ page }) => {
    const uniqueSku = `E2E-${Date.now()}`;

    await page.goto(BASE_URL);
    await page.getByTestId('email-input').fill('jihad@admin');
    await page.getByTestId('password-input').fill('123456');
    await page.getByTestId('login-btn').click();

    await expect(page.getByTestId('logout-btn')).toBeVisible();

    // Ajout d'un produit avec stock = 10
    await page.getByTestId('prod-name').fill('Écran Dell 27');
    await page.getByTestId('prod-sku').fill(uniqueSku);
    await page.getByTestId('prod-desc').fill('Écran IPS');
    await page.getByTestId('prod-price').fill('650');
    await page.getByTestId('prod-stock').fill('10');
    await page.getByTestId('add-prod-btn').click();

    const productRow = page.getByTestId(`product-row-${uniqueSku}`);
    await expect(productRow).toBeVisible();
    await expect(productRow).toContainText('10');

    // Passation d'une commande de 3 unités
    await page.getByTestId('order-product-select').selectOption({ label: `Écran Dell 27 (${uniqueSku}) - Stock: 10` });
    await page.getByTestId('order-qty').fill('3');
    await page.getByTestId('create-order-btn').click();

    // Vérification de la baisse du stock à 7
    await expect(productRow).toContainText('7');

    // Confirmation de la commande
    await page.getByRole('button', { name: 'Confirmer' }).last().click();
    await expect(page.locator('span', { hasText: 'CONFIRMED' }).last()).toBeVisible();
  });
});