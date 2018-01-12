package ast.binary;

public class Case
{
	public final ast.PayloadType pay;
	public final Type body;
	
	public Case(ast.PayloadType pay, Type body)
	{
		this.pay = pay;
		this.body = body;
	}
	
	@Override
	public String toString()
	{
		return "(" + ((this.pay == null) ? "" : this.pay) + ")." + this.body;
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((body == null) ? 0 : body.hashCode());
		result = prime * result + ((pay == null) ? 0 : pay.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (!(obj instanceof Case))
		{
			return false;
		}
		Case other = (Case) obj;
		if (body == null)
		{
			if (other.body != null)
			{
				return false;
			}
		} else if (!body.equals(other.body))
		{
			return false;
		}
		if (pay == null)
		{
			if (other.pay != null)
			{
				return false;
			}
		} else if (!pay.equals(other.pay))
		{
			return false;
		}
		return true;
	}
}
