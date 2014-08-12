package scott.data.model;

import java.math.BigDecimal;
import java.util.Date;

public interface Month {

	Long getId();
	
	Date getStarting();
	
	void setStarting(Date value);
	
	BigDecimal getStartingBalance();
	
	void setStartingBalance(BigDecimal value);
	
	Boolean getFinished();
	
	void setFinished(Boolean finished);
}
