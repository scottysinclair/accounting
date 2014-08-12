package scott.data.model;

public interface Category {
	Long getId();

	String getName();
	
	void setName(String name);
	
	Integer getMonthlyLimit();

	void setMonthlyLimit(Integer limit);

}
