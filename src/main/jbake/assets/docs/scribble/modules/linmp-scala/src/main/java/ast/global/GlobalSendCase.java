package ast.global;

import ast.PayloadType;


public class GlobalSendCase
{
	public final PayloadType pay;
	public final GlobalType body;
	
	public GlobalSendCase(PayloadType pay, GlobalType body)
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
		if (!(obj instanceof GlobalSendCase))
		{
			return false;
		}
		GlobalSendCase other = (GlobalSendCase) obj;
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
