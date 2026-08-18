package com.example.practicetests.delete;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;


public class SeleniumPractice {
	
	public static void main(String [] args ) throws InterruptedException, IOException {
		
		
		  WebDriver driver = new ChromeDriver();
		 
		  try {
				  driver.get("https://rahulshettyacademy.com/client/#/auth/login");
				  driver.manage().window().maximize();
				  driver.findElement(By.cssSelector("#userEmail")).sendKeys("mj@something.com");
				  driver.findElement(By.cssSelector("#userPassword")).sendKeys("Masakela1!");
				  driver.findElement(By.cssSelector("#login")).click();
				  
				  WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
				  wait.until(ExpectedConditions.urlContains("dashboard"));
				  
				  //System.out.println(driver.getCurrentUrl());
				  Assert.assertEquals(driver.getCurrentUrl(),
						  "https://rahulshettyacademy.com/client/#/dashboard/dash");
				  
				  List<WebElement> cards = driver.findElements(By.xpath("//div[@class='card-body']"));
				  System.out.println("Number of cards: " + cards.size());
				  
				  if(cards.size()>0) {
					  for (WebElement card : cards) {
						  String name = card.findElement(By.xpath(".//h5/b")).getText();
						  String price = card.findElement(By.xpath(".//div[@class='text-muted']")).getText();
						  System.out.println(name);
						  System.out.println(price);
					  }
				  }
		  //Thread.sleep(3000);
		  }finally { 
			  driver.quit();
		  }
		 
	}
}