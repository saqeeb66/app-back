package com.arcotcabs.arcotcabs_backend.repository;

import com.arcotcabs.arcotcabs_backend.model.User;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.Map;

@Repository
public class UserRepository {

    private final DynamoDbClient dynamoDbClient;
    private static final String TABLE_NAME = "Users";

    public UserRepository(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    public void save(User user) {

        dynamoDbClient.putItem(
                PutItemRequest.builder()
                        .tableName(TABLE_NAME)
                        .item(Map.of(
                                "userId", AttributeValue.fromS(user.getUserId()),
                                "name", AttributeValue.fromS(user.getName()),
                                "email", AttributeValue.fromS(user.getEmail()),
                                "phone", AttributeValue.fromS(user.getPhone()),
                                "passwordHash", AttributeValue.fromS(user.getPasswordHash()),
                                "role", AttributeValue.fromS(user.getRole()),
                                "createdAt", AttributeValue.fromN(String.valueOf(user.getCreatedAt()))
                        ))
                        .build()
        );
    }
    public boolean existsByEmail(String email) {
        return findByUserId(email) != null;
    }

    public User findByUserId(String userId) {

        var response = dynamoDbClient.getItem(
                GetItemRequest.builder()
                        .tableName(TABLE_NAME)
                        .key(Map.of(
                                "userId", AttributeValue.fromS(userId)
                        ))
                        .build()
        );

        if (!response.hasItem()) return null;

        var item = response.item();

        User user = new User();
        user.setUserId(item.get("userId").s());
        user.setName(item.get("name").s());
        user.setEmail(item.get("email").s());
        user.setPhone(item.get("phone").s());
        user.setPasswordHash(item.get("passwordHash").s());
        user.setRole(item.get("role").s());
        user.setCreatedAt(Long.parseLong(item.get("createdAt").n()));

        return user;
    }
}

