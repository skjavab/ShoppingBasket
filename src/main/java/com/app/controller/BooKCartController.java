package com.app.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.app.common.constant.ApiConstant;
import com.app.entity.Book;
import com.app.entity.BooksDiscountDetails;
import com.app.model.BillingDetails;
import com.app.model.ShoppingCartItem;
import com.app.services.BookCartService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Class: BooKCartController
 *
 */
@RestController
@Api("Book Cart")
public class BooKCartController {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	BookCartService service;

	@PostMapping(value = ApiConstant.APPI_URL)
	public int saveBook(final @RequestBody Book book) {
		log.info("Save book details");
		service.saveBook(book);
		return book.getId();
	}

	@GetMapping(value = ApiConstant.APPI_URL, produces = "application/json")
	@ApiOperation("Fetch Books data")
	public List<Book> getAllBooks() {

		log.info("Getting book details from the database.");
		return service.getAllBooks();
	}

	@PostMapping(value = ApiConstant.DISCOUNT_URL)
	public String saveDiscounts(final @RequestBody BooksDiscountDetails discountItemInfo) {
		log.info("Saving book details in the database.");
		service.saveBooksDiscountDetails(discountItemInfo);
		return "Saving DiscountDetails details in the database.";
	}

	@ApiOperation("Fetch Book DiscountDetails")
	@GetMapping(value = ApiConstant.DISCOUNT_URL, produces = "application/json")
	public List<BooksDiscountDetails> getAllBookDiscountDetails() {

		log.info("Getting book details from the database.");
		return service.getAllBookDiscountDetails();
	}

	@ApiOperation("Calculate total Price of Book Cart")
	@PostMapping(value = ApiConstant.CALCULATEPRICE_URL)
	public BillingDetails calculatePrice(@RequestBody List<ShoppingCartItem> shoppingCartItem) {

		log.info("Saving book details in the database.");

		return service.calculatePrice(shoppingCartItem);
	}
	 
}
