package scott.data.query;

import scott.data.model.Account;

import com.smartstream.sodit.api.query.QProperty;
import com.smartstream.sodit.api.query.QueryObject;

public class QAccount extends QueryObject<Account> {

	private static final long serialVersionUID = 1L;

	public QAccount() {
		super(Account.class);
	}

	public QAccount(QueryObject<?> parent) {
		super(Account.class, parent);
	}

	public QProperty<String> name() {
		return new QProperty<String>(this, "name");
	}

}
