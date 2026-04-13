// MongoDB Initialization Script for NutriScan
// Usage: mongosh "mongodb://localhost:27017/nutriscan" --file init_mongodb.js
// Or if using auth: mongosh "mongodb://luminous:X4Gms1AZtzGmzULe@118.195.219.166:27017/nutriscan?authSource=nutriscan" --file init_mongodb.js

// Switch to the database (in case not connected directly)
db = db.getSiblingDB('nutriscan');

print("Initializing 'nutriscan' database...");

// ---------------------------------------------------------
// 1. Users Collection
// ---------------------------------------------------------
print("Creating 'users' collection...");
if (!db.getCollectionNames().includes('users')) {
    db.createCollection('users');
}

// Indexes for Users
// Unique index on phoneNumber, but allows null/missing values (sparse/partial)
db.users.createIndex(
    { "phoneNumber": 1 }, 
    { 
        unique: true, 
        name: "idx_users_phone",
        partialFilterExpression: { phoneNumber: { $exists: true, $type: "string" } }
    }
);

// Unique index on wechatOpenId (REMOVED as WeChat login is deprecated)
// db.users.createIndex(
//     { "wechatOpenId": 1 }, 
//     { 
//         unique: true, 
//         name: "idx_users_wechat",
//         partialFilterExpression: { wechatOpenId: { $exists: true, $type: "string" } }
//     }
// );

print("Users collection initialized.");

// ---------------------------------------------------------
// 2. Posts Collection
// ---------------------------------------------------------
print("Creating 'posts' collection...");
if (!db.getCollectionNames().includes('posts')) {
    db.createCollection('posts');
}

// Indexes for Posts
db.posts.createIndex({ "category": 1 }, { name: "idx_posts_category" });
db.posts.createIndex({ "authorId": 1 }, { name: "idx_posts_author" });
db.posts.createIndex({ "createdAt": -1 }, { name: "idx_posts_created_desc" }); // For feed sorting
db.posts.createIndex({ "status": 1 }, { name: "idx_posts_status" });

print("Posts collection initialized.");

// ---------------------------------------------------------
// 3. Comments Collection
// ---------------------------------------------------------
print("Creating 'comments' collection...");
if (!db.getCollectionNames().includes('comments')) {
    db.createCollection('comments');
}

// Indexes for Comments
db.comments.createIndex({ "postId": 1 }, { name: "idx_comments_post" });
db.comments.createIndex({ "createdAt": 1 }, { name: "idx_comments_created_asc" }); // Oldest first usually

print("Comments collection initialized.");

// ---------------------------------------------------------
// Summary
// ---------------------------------------------------------
print("---------------------------------------------------");
print("Database initialization completed successfully.");
print("Collections: " + db.getCollectionNames().join(", "));
