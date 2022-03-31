
package com.app.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.entity.Book;
import com.app.entity.BooksDiscountDetails;
import com.app.model.BillingDetails;
import com.app.model.BooksSet;
import com.app.model.ShoppingCartItem;
import com.app.repository.BookRepository;
import com.app.repository.BooksDiscountRepository;
import com.app.services.BookCartService;

@Service
public class BookCartServiceImpl implements BookCartService {

	// @Autowired annotation provides the automatic dependency injection.
	@Autowired
	BookRepository repository;
	@Autowired
	BooksDiscountRepository booksDiscountRepository;

	// Save student entity in the h2 database.
	public void saveBook(final Book book) {
		repository.save(book);
	}

	// Get all students from the h2 database.
	public List<Book> getAllBooks() {
		final List<Book> books = new ArrayList<>();
		repository.findAll().forEach(book -> books.add(book));
		return books;
	}

	@Override
	public void saveBooksDiscountDetails(BooksDiscountDetails discountItemInfo) {
		booksDiscountRepository.save(discountItemInfo);

	}

	@Override
	public List<BooksDiscountDetails> getAllBookDiscountDetails() {
		final ArrayList<BooksDiscountDetails> booksDiscountDetails = new ArrayList<BooksDiscountDetails>();
		booksDiscountRepository.findAll().forEach(discountItemInfo -> booksDiscountDetails.add(discountItemInfo));

		return booksDiscountDetails;
	}

	@Override
	public BillingDetails calculatePrice(List<ShoppingCartItem> shoppingCartItem) {
		BillingDetails billingDetails = new BillingDetails();
		List<BooksSet> setsOfDifferentBooks = getDifferentBooksSetsWithMaxTotalDiscount(shoppingCartItem);

		double totalPrice = 0.0;
		double setPrice = 0.0;

		for (BooksSet booksSet : setsOfDifferentBooks) {
			for (Book book : booksSet.getBooks()) {
				setPrice += book.getPrice();

			}

			setPrice = setPrice * (1.0 - (booksSet.getDiscount() / 100.0));
			totalPrice += setPrice;
			setPrice = 0;
		}
		billingDetails.setSetsOfDifferentBooks(setsOfDifferentBooks);
		billingDetails.setTotalAmount(totalPrice + "");
		return billingDetails;
	}

	public List<BooksSet> getDifferentBooksSetsWithMaxTotalDiscount(List<ShoppingCartItem> shoppingCartItems) {

		List<BooksSet> optimizeSetList;
		List<List<BooksSet>> differentBooksSetsCombinations = new ArrayList<>();

		differentBooksSetsCombinations = IntStream.range(0, shoppingCartItems.size())
				.mapToObj(
						i -> calculateDifferentBooksSetsByMaxSize(shoppingCartItems, shoppingCartItems.size() + 1 - i))
				.collect(Collectors.toList());

		if (differentBooksSetsCombinations.size() > 1) {
			optimizeSetList = selectBooksSetsWithMaxDiscount(differentBooksSetsCombinations);
		} else {
			optimizeSetList = differentBooksSetsCombinations.get(0);
		}

		return optimizeSetList;
	}

	private List<BooksSet> calculateDifferentBooksSetsByMaxSize(List<ShoppingCartItem> shoppingCartItems,
			int maxSizeSet) {

		Map<Integer, Book> bookMap = new HashMap<>();
		repository.findAll().forEach(book -> bookMap.put(book.getId(), book));
		List<ShoppingCartItem> remainingShoppingCartItems = new ArrayList<ShoppingCartItem>();
		List<BooksSet> setsOfDifferentBooks = new ArrayList<>();
		shoppingCartItems.stream().forEach(item -> {
			remainingShoppingCartItems.add(new ShoppingCartItem(item.getBook(), item.getQuantity()));
		});
		while (remainingShoppingCartItems.size() > 0) {

			HashSet<Book> books = new HashSet<>();

			for (ShoppingCartItem item : new ArrayList<>(remainingShoppingCartItems)) {
				Book book = item.getBook();
				book.setPrice(bookMap.get(book.getId()).getPrice());
				book.setTitle(bookMap.get(book.getId()).getTitle());
				book.setAuthor(bookMap.get(book.getId()).getAuthor());
				book.setYear(bookMap.get(book.getId()).getYear());
				books.add(book);

				if (item.getQuantity() == 1) {
					remainingShoppingCartItems.remove(item);
				} else {
					item.changeQuantity(item.getQuantity() - 1);
				}
				if (books.size() == maxSizeSet) {
					break;
				}
			}

			BooksSet booksSet = new BooksSet(books, getDiscount(books.size()));

			setsOfDifferentBooks.add(booksSet);
		}

		return setsOfDifferentBooks;
	}

	private List<BooksSet> selectBooksSetsWithMaxDiscount(List<List<BooksSet>> booksSetsCombinations) {
		List<BooksSet> maxDiscountBooksSets = null;
		int maxBooksSetsDiscount = 0;
		int totalBooksSetsDiscount = 0;

		for (List<BooksSet> booksSets : booksSetsCombinations) {
			for (BooksSet booksSet : booksSets) {
				totalBooksSetsDiscount += booksSet.getDiscount();
			}

			if (maxBooksSetsDiscount < totalBooksSetsDiscount) {
				maxDiscountBooksSets = booksSets;
				maxBooksSetsDiscount = totalBooksSetsDiscount;
			}

			totalBooksSetsDiscount = 0;
		}

		return maxDiscountBooksSets;
	}

	private int getDiscount(int differentBooksCount) {
		int defaultDiscount = 0;
		List<BooksDiscountDetails> discounts = getAllBookDiscountDetails();
		for (BooksDiscountDetails discount : discounts) {
			if (differentBooksCount == discount.getDifferentCopies())
				return discount.getDiscount();
		}

		return defaultDiscount;
	}
}
