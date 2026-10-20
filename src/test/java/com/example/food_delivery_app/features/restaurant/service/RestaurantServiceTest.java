package com.example.food_delivery_app.features.restaurant.service;

import com.example.food_delivery_app.core.exceptions.ResourceNotFoundException;
import com.example.food_delivery_app.features.restaurant.dto.CreateRestaurantRequest;
import com.example.food_delivery_app.features.restaurant.dto.RestaurantMapper;
import com.example.food_delivery_app.features.restaurant.dto.RestaurantResponse;
import com.example.food_delivery_app.features.restaurant.entity.Category;
import com.example.food_delivery_app.features.restaurant.entity.Restaurant;
import com.example.food_delivery_app.features.restaurant.repository.CategoryRepository;
import com.example.food_delivery_app.features.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;



@ExtendWith(MockitoExtension.class)
@DisplayName("RestaurantService Unit Tests")
class RestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private RestaurantMapper mapper;

    @InjectMocks
    private RestaurantService restaurantService;

    private Category category;
    private Restaurant restaurant;
    private CreateRestaurantRequest createRestaurantRequest;
    private RestaurantResponse restaurantResponse;


    @BeforeEach
    void setUp() {
        category = new Category().builder()
                .id(1)
                .name("Fast Food")
                .build();


        restaurant = new Restaurant().builder()
                .id(1)
                .name("Test Restaurant")
                .description("A test restaurant")
                .category(category)
                .address("123 Test St")
                .phone("+201234567890")
                .open(true)
                .build();

        createRestaurantRequest = CreateRestaurantRequest.builder()
                .name("Test Restaurant")
                .description("A test restaurant")
                .categoryId(1)
                .address("123 Test St")
                .phone("+201234567890")
                .open(true)
                .build();

        restaurantResponse = RestaurantResponse.builder()
                .id(1)
                .name("Test Restaurant")
                .description("A test restaurant")
                .categoryId(1)
                .address("123 Test St")
                .phone("+201234567890")
                .open(true)
                .build();
    }

    @Nested
    @DisplayName("create method")
    class CreateMethodTests {
        @Test
        @DisplayName("should create a restaurant successfully")
        void testCreateRestaurantSuccessfully() {
            // Given
            when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
            when(mapper.toEntity(createRestaurantRequest, category)).thenReturn(restaurant);
            when(restaurantRepository.save(any(Restaurant.class))).thenReturn(restaurant);
            when(mapper.toResponse(restaurant)).thenReturn(restaurantResponse);

            // when
            final Integer restaurantId = restaurantService.create(createRestaurantRequest).id();

            // Then
            assertNotNull(restaurantId);
            assertEquals(1, restaurantId);
            verify(categoryRepository, times(1)).findById(1);
            verify(mapper, times(1)).toEntity(createRestaurantRequest, category);
            verify(restaurantRepository, times(1)).save(any(Restaurant.class));


            // Verify that category is set on the restaurant entity
            verify(restaurantRepository).save(argThat(savedRestaurant ->
                    savedRestaurant.getCategory()!=null && savedRestaurant.getCategory().getId().equals(1)));

        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when category does not exist")
        void testCreateRestaurantCategoryNotFound() {
            // Given
            when(categoryRepository.findById(createRestaurantRequest.categoryId())).thenReturn(Optional.empty());

            // When & Then
            assertThrows(ResourceNotFoundException.class, () -> restaurantService.create(createRestaurantRequest));
            verify(categoryRepository, times(1)).findById(createRestaurantRequest.categoryId());
            verifyNoInteractions(mapper);
            verifyNoInteractions(restaurantRepository);
        }
    }


}