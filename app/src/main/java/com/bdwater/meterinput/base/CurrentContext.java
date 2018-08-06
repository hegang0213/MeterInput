package com.bdwater.meterinput.base;

import com.bdwater.meterinput.model.Book;
import com.bdwater.meterinput.model.FakeTitle;
import com.bdwater.meterinput.model.FakeWaterPrice;
import com.bdwater.meterinput.model.Meter;
import com.bdwater.meterinput.model.User;
import com.bdwater.meterinput.model.WaterStatus;

import java.util.ArrayList;

/**
 * Created by hegang on 16/6/13.
 */
public class CurrentContext {
    private User user;
    private Book[] books;
    private WaterStatus[] waterStatuses;
    private FakeTitle[] fakeTitles;
    private FakeWaterPrice[] fakeWaterPrices;

    private Meter[] meters;
    private Book currentBook;
    private Meter currentMeter;

    public void setUser(User user) {
        this.user = user;
    }
    public User getUser() {
        return user;
    }

    // book
    public void setBooks(Book[] books) {
        this.books = books;
    }
    public Book[] getBooks() {
        return books;
    }
    public void setCurrentBook(Book currentBook) {
        this.currentBook = currentBook;
    }
    public Book getCurrentBook() {
        return currentBook;
    }
    public Boolean containsBook(Book book) {
        if(null == books) return false;

        for(Book b : books) {
            if(b.BookID.equals(book.BookID))
                return true;
        }
        return false;
    }
    public Book getBookById(String bookId) {
        if(null == books) return null;
        for(Book b : books) {
            if(b.BookID.equals(bookId))
                return b;
        }
        return null;
    }
    public Book getBook(int index) {
        if(books == null) return null;
        return books[index];
    }
    public int getBookPosition(Book book) {
        int result = -1;
        if(book == null) return result;
        if(books == null) return result;

        for(int i = 0; i < books.length; i++) {
            if(book.BookID.equals(books[i].BookID))
                return i;
        }
        return result;
    }

    // meter
    public Meter getPrevMeter() {
        int position = getMeterPosition(getCurrentMeter());
        if(position - 1 < 0)
            return null;
        return getMeters()[position - 1];
    }
    public Meter getNextMeter() {
        int position = getMeterPosition(getCurrentMeter());
        if((position + 1) >= getMeters().length)
            return null;
        return getMeters()[position + 1];
    }
    public int getMeterPosition(Meter meter) {
        int result = -1;
        if(meter == null) return result;
        if(meters == null) return result;

        for(int i = 0; i < meters.length; i++) {
            if(meter.MeterID.equals(meters[i].MeterID))
                return i;
        }
        return result;
    }
    public void setCurrentMeter(Meter currentMeter) {
        this.currentMeter = currentMeter;
    }
    public Meter getCurrentMeter() {
        return currentMeter;
    }
    public void setMeters(Meter[] meters) {
        this.meters = meters;
    }
    public Meter[] getMeters() {
        return meters;
    }

    public int indexOfWaterStatuses(String key) {
        if(waterStatuses == null) return -1;
        for(int i = 0; i < waterStatuses.length; i++) {
            if(waterStatuses[i].Title.equals(key))
            {
                return i;
            }

        }
        return -1;
    }
    public WaterStatus[] getWaterStatuses() {
        return waterStatuses;
    }

    public void setWaterStatuses(WaterStatus[] waterStatuses) {
        this.waterStatuses = waterStatuses;
    }

    public FakeTitle[] getFakeTitles() {
        return fakeTitles;
    }
    public void setFakeTitles(FakeTitle[] fakeTitles) {
        this.fakeTitles = fakeTitles;
    }

    public void setFakeWaterPrices(FakeWaterPrice[] fakeWaterPrices) {
        this.fakeWaterPrices = fakeWaterPrices;
    }
    public FakeWaterPrice[] getFakeWaterPrices() {
        return fakeWaterPrices;
    }
    public int indexOfFakeWaterPrice(String waterPriceID) {
        for(int i = 0; i < fakeWaterPrices.length; i++) {
            if(fakeWaterPrices[i].WaterPriceID.equals(waterPriceID))
                return i;
        }
        return 0;
    }
    public FakeWaterPrice[] getFakeWaterPricesGreatThen(double price) {
        ArrayList<FakeWaterPrice> result = new ArrayList<FakeWaterPrice>();
        for(int i = 0; i < fakeWaterPrices.length; i++){
            if(fakeWaterPrices[i].Price > price) {
                result.add(fakeWaterPrices[i]);
            }
        }
        return (FakeWaterPrice[])result.toArray(new FakeWaterPrice[result.size()]);
    }
}
