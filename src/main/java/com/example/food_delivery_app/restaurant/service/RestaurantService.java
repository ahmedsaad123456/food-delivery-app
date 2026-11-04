package com.example.food_delivery_app.restaurant.service;

import com.example.food_delivery_app.common.Constants;
import com.example.food_delivery_app.common.ErrorCode;
import com.example.food_delivery_app.common.PageQuery;
import com.example.food_delivery_app.common.PageResponse;
import com.example.food_delivery_app.exceptions.AppException;
import com.example.food_delivery_app.exceptions.ResourceNotFoundException;
import com.example.food_delivery_app.restaurant.dto.restaurant.CreateRestaurantRequest;
import com.example.food_delivery_app.restaurant.dto.restaurant.RestaurantListItem;
import com.example.food_delivery_app.restaurant.dto.restaurant.RestaurantSummary;
import com.example.food_delivery_app.restaurant.mapper.RestaurantMapper;
import com.example.food_delivery_app.restaurant.dto.restaurant.RestaurantResponse;
import com.example.food_delivery_app.restaurant.entity.Category;
import com.example.food_delivery_app.restaurant.entity.Restaurant;
import com.example.food_delivery_app.restaurant.repository.CategoryRepository;
import com.example.food_delivery_app.restaurant.repository.RestaurantRepository;
import com.example.food_delivery_app.restaurant.specification.RestaurantSearchCriteria;
import com.example.food_delivery_app.restaurant.specification.RestaurantSpecs;
import com.example.food_delivery_app.users.entity.User;
import com.example.food_delivery_app.users.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final RestaurantMapper mapper;
    private static final Set<String> SORTABLE = Set.of("name", "createdAt", "id");



    @Transactional
    public RestaurantResponse createRestaurant(CreateRestaurantRequest req , String ownerEmail) {
        Category category = categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException(Constants.CATEGORY, req.categoryId()));

        // service.create(request, String ownerEmail)
        User owner = userRepository.findByEmail(ownerEmail).orElseThrow(() -> new ResourceNotFoundException(Constants.USER, ownerEmail));
        Restaurant restaurant = mapper.toRestaurantEntity(req, category);
        restaurant.setOwner(owner);
        Restaurant saved = restaurantRepository.save(restaurant);
        return mapper.toRestaurantResponse(saved);
    }

    @Transactional(readOnly = true)

    public RestaurantResponse getRestaurantById(Integer id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.RESTAURANT, id));
        return mapper.toRestaurantResponse(restaurant);
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponse> getAllRestaurants() {
        return mapper.toListRestaurantResponse(
                restaurantRepository.findAll()
        );
    }

    @Transactional
    public RestaurantResponse updateRestaurant(Integer id, CreateRestaurantRequest req) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.RESTAURANT, id));

        Category category = categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException(Constants.CATEGORY, req.categoryId()));

        restaurant.setName(req.name());
        restaurant.setDescription(req.description());
        restaurant.setAddress(req.address());
        restaurant.setPhone(req.phone());
        restaurant.setOpen(req.open());
        restaurant.setCategory(category);

        Restaurant updated = restaurantRepository.save(restaurant);
        return mapper.toRestaurantResponse(updated);
    }

    @Transactional
    public void deleteRestaurant(Integer id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.RESTAURANT, id));
        restaurantRepository.delete(restaurant);
    }

    @Transactional(readOnly = true)
    public PageResponse<RestaurantListItem> searchForRestaurants(RestaurantSearchCriteria c, PageQuery pageQuery) {
        // default values for pageQuery if not provided
        Pageable pageable = pageQuery.toPageable(SORTABLE, Constants.NAME, Sort.Direction.ASC);

        Specification<Restaurant> spec = Specification.allOf(
                RestaurantSpecs.nameContains(c.name()),
                RestaurantSpecs.category(c.categoryId()),
                RestaurantSpecs.openOnly(c.openOnly()));

        Page<RestaurantSummary> page =
                restaurantRepository.findBy(spec, q -> q.as(RestaurantSummary.class).page(pageable));

        return PageResponse.from(page.map(RestaurantListItem::from));
    }

}