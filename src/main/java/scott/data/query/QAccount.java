package scott.data.query;

import scott.barleydb.api.query.JoinType;
import scott.barleydb.api.query.QProperty;
import scott.barleydb.api.query.QueryObject;
import scott.data.model.Account;

/**
 * Generated from Entity Specification
 *
 * @author scott
 */
public class QAccount extends QueryObject<Account> {
  private static final long serialVersionUID = 1L;
  public QAccount() {
    super(Account.class);
  }

  public QAccount(QueryObject<?> parent) {
    super(Account.class, parent);
  }


  public QProperty<Long> id() {
    return new QProperty<Long>(this, "id");
  }

  public QProperty<String> name() {
    return new QProperty<String>(this, "name");
  }
}