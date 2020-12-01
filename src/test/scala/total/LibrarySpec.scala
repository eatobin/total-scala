package total

import org.scalatest.flatspec.AnyFlatSpec
import total.Book._
import total.Borrower._
import total.Library._

class LibrarySpec extends AnyFlatSpec {

  val br1: Borrower = Borrower("Borrower1", 1)
  val br2: Borrower = Borrower("Borrower2", 2)
  val br3: Borrower = Borrower("Borrower3", 3)

  val brs1: List[Borrower] = List(br1, br2)
  val brs2: List[Borrower] = List(br3, br1, br2)

  val bk1: Book = Book("Title1", "Author1", Some(br1))
  val bk2: Book = Book("Title2", "Author2", None)
  val bk3: Book = Book("Title3", "Author3", Some(br3))
  val bk4: Book = Book("Title4", "Author4", Some(Borrower("Borrower3", 3)))

  val bks1: List[Book] = List(bk1, bk2)
  val bks2: List[Book] = List(bk3, bk1, bk2)
  val bks3: List[Book] = List(bk1, bk2, bk3, bk4)

  val jsonStringBorrowers = "[{\n  \"maxBooks\": 1,\n  \"name\": \"Borrower1\"\n}, {\n  \"maxBooks\": 2,\n  \"name\": \"Borrower2\"\n}]"
  val jsonStringBooks = "[{\n  \"author\": \"Author22\",\n  \"borrower\": null,\n  \"title\": \"Title2\"\n}, {\n  \"author\": \"Author99\",\n  \"borrower\": null,\n  \"title\": \"Title99\"\n}]"
  val jsonStringBorrowersBad = "[{\"nameX\":\"Borrower1\",\"maxBooks\":1},{\"name\":\"Borrower2\",\"maxBooks\":2}]"
  val jsonStringBooksBad = "[{\"titleX\":\"Title2\",\"author\":\"Author22\",\"borrower\":null},{\"title\":\"Title99\",\"author\":\"Author99\",\"borrower\":null}]"
  val jsonStringBorrowersBad2 = "[{\"name\"\"Borrower1\",\"max-books\":1},{\"name\":\"Borrower2\",\"max-books\":2}]"

  val ss = "\n--- Status Report of Test Library ---\n\nTest Library: 3 books; 3 borrowers.\n\nTitle3 by Author3; Checked out to Borrower3\nTitle1 by Author1; Checked out to Borrower1\nTitle2 by Author2; Available\n\nBorrower3 (3 books)\nBorrower1 (1 books)\nBorrower2 (2 books)\n\n--- End of Status Report ---\n"

  "A Library" should "add a Borrower or Book correctly" in {
    assert(addItem(br3, brs1) == brs2)
    assert(addItem(br2, brs1) == brs1)

    assert(addItem(bk3, bks1) == bks2)
    assert(addItem(bk2, bks1) == bks1)
  }

  it should "remove a Book correctly" in {
    assert(removeBook(bk3, bks2) == bks1)
    assert(removeBook(bk3, bks1) == bks1)
  }

  it should "find a Book or Borrower correctly" in {
    assert(findItem("Title1", bks2, getTitle).contains(bk1))
    assert(findItem("Title11", bks2, getTitle).isEmpty)

    assert(findItem("Borrower1", brs2, getName).contains(br1))
    assert(findItem("Borrower11", brs2, getName).isEmpty)
  }

  it should "find List[Book] for a Borrower" in {
    assert(getBooksForBorrower(br2, bks1) == List())
    assert(getBooksForBorrower(br1, bks1) == List(bk1))
    assert(getBooksForBorrower(br3, bks3) == List(bk3, bk4))
  }

  it should "check out a Book correctly" in {
    assert(checkOut("Borrower2", "Title1", brs1, bks1) == bks1)
    assert(checkOut("Borrower2", "NoTitle", brs1, bks1) == bks1)
    assert(checkOut("NoName", "Title1", brs1, bks1) == bks1)
    assert(checkOut("Borrower1", "Title2", brs1, bks1) == bks1)
    assert(checkOut("Borrower2", "Title2", brs1, bks1) ==
      List(Book("Title2", "Author2", Some(Borrower("Borrower2", 2))), bk1))
  }

  it should "check in a Book correctly" in {
    assert(checkIn("Title1", bks1) == List(Book("Title1", "Author1", None), bk2))
    assert(checkIn("Title2", bks1) == bks1)
    assert(checkIn("NoTitle", bks1) == bks1)
  }

  it should "parse json strings to objects" in {
    assert(jsonStringToBorrowers(Right(jsonStringBorrowers)) ==
      Right(List(Borrower("Borrower1", 1), Borrower("Borrower2", 2))))
    assert(jsonStringToBooks(Right(jsonStringBooks)) ==
      Right(List(Book("Title2", "Author22", None), Book("Title99", "Author99", None))))
  }

  it should "report json parse errors" in {
    val badParse1 = jsonStringToBorrowers(Right(jsonStringBorrowersBad))
    val result1 = badParse1 match {
      case Left(_) => "Invalid JSON"
      case Right(_) => "Yay, got some JSON!"
    }
    assert(result1 == "Invalid JSON")
    val badParse2 = jsonStringToBorrowers(Right(jsonStringBorrowersBad2))
    val result2 = badParse2 match {
      case Left(_) => "Invalid JSON again"
      case Right(_) => "Yay, got some JSON!"
    }
    assert(result2 == "Invalid JSON again")
    val badParse3 = jsonStringToBooks(Right(jsonStringBooksBad))
    val result3 = badParse3 match {
      case Left(_) => "Invalid JSON - yeesh"
      case Right(_) => "Yay, got some JSON!"
    }
    assert(result3 == "Invalid JSON - yeesh")
  }

  //  it should "report read file errors" in {
  //    val s1 = Main.readFileIntoJsonString("noFile.json")
  //    assert(jsonStringToBorrowers(s1) == Left("noFile.json (No such file or directory)"))
  //    val s2 = Main.readFileIntoJsonString("src/main/resources/empty.json")
  //    assert(jsonStringToBorrowers(s2) == Right(List()))
  //  }

  it should "convert objects to json strings" in {
    assert(borrowersToJsonString(List(Borrower("Borrower1", 1), Borrower("Borrower2", 2))) ==
      """[{"name":"Borrower1","maxBooks":1},{"name":"Borrower2","maxBooks":2}]""")
    assert(booksToJsonString(List(Book("Title2", "Author22", None), Book("Title99", "Author99", None))) ==
      """[{"title":"Title2","author":"Author22","borrower":null},{"title":"Title99","author":"Author99","borrower":null}]""")
  }

  it should "print out a Status report" in {
    assert(statusToString(bks2, brs2) == ss)
  }

}
