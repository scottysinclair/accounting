package scott.data.query;

import java.util.Date;

import scott.data.model.Transaction;

import com.smartstream.messaging.query.QXMLStructure;
import com.smartstream.sodit.api.query.QJoin;
import com.smartstream.sodit.api.query.QProperty;
import com.smartstream.sodit.api.query.QueryObject;

public class QTransaction extends QueryObject<Transaction> {

	private static final long serialVersionUID = 1L;

	public QTransaction() {
		super(Transaction.class);
	}

	public QTransaction(QueryObject<?> parent) {
		super(Transaction.class, parent);
	}

	public QCategory joinToCategory() {
	    QCategory category = new QCategory();
	    addJoin(category, "category");
	    return category;
	}

	public QAccount joinToAccount() {
	    QAccount account = new QAccount();
	    addJoin(account, "account");
	    return account;
	}

	public QProperty<Long> accountId() {
	  return new QProperty<Long>(this, "account");
	}

	public QProperty<Date> date() {
	  return new QProperty<Date>(this, "date");
	}

}
