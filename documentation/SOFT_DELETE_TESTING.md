# Soft Delete Testing Guide

This guide explains how to verify the Soft Delete functionality for Products using Postman or `curl`.

## Prerequisites
- Application must be running (`mvn spring-boot:run`).
- You must have an Admin account (e.g., `admin2` / `admin123`).

---

## 1. Authentication (Get Token)
**Endpoint:** `POST /auth/login`
**Body:**
```json
{
    "usernameOrEmail": "admin2",
    "password": "admin123"
}
```
**Action:** Copy the `token` from the response. You will use this as a Bearer Token for all subsequent requests.

---

## 2. Create a Product (To be deleted)
**Endpoint:** `POST /products`
**Header:** `Authorization: Bearer <YOUR_TOKEN>`
**Body:**
```json
{
    "code": "SD-TEST-NEW-001",
    "name": "Product To Delete",
    "minAmount": 5000000,
    "maxAmount": 10000000,
    "minTenureMonths": 12,
    "maxTenureMonths": 24,
    "interestRate": 5.5,
    "interestRateType": "FIXED",
    "isActive": true
}
```
**Expected Response (201 Created):**
Note the `id` of the created product (e.g., `105`).

---

## 3. Verify Product Exists
**Endpoint:** `GET /products`
**Header:** `Authorization: Bearer <YOUR_TOKEN>`

**Action:** Search for code `SD-TEST-001`.
**Expected Result:** The product should be present in the list.

---

## 4. Soft Delete the Product
**Endpoint:** `DELETE /products/{id}` (e.g., `/products/105`)
**Header:** `Authorization: Bearer <YOUR_TOKEN>`

**Expected Response (200 OK):**
```json
{
    "success": true,
    "message": "Product deleted successfully",
    "data": null,
    ...
}
```

---

## 5. Verify Product is "Gone"
**Endpoint:** `GET /products`
**Header:** `Authorization: Bearer <YOUR_TOKEN>`

**Action:** Search for code `SD-TEST-001`.
**Expected Result:** The product should **NOT** be in the list anymore.

### (Optional) Database Verification
If you check the database directly:
```sql
SELECT * FROM products WHERE code = 'SD-TEST-001';
```
**Result:** The row should still exist, but `is_deleted` will be `1` (true) and `is_active` will be `0` (false).
