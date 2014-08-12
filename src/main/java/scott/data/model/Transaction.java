package scott.data.model;

import java.math.BigDecimal;
import java.util.Date;

public interface Transaction {

	Long getId();

	Account getAccount();

	void setAccount(Account account);

	Date getDate();

	void setDate(Date date);

	BigDecimal getAmount();

	void setAmount(BigDecimal amount);

	Category getCategory();

	void setCategory(Category category);

	String getComment();

	void setComment(String comment);

	Boolean getImportant();

	void setImportant(Boolean important);
}
