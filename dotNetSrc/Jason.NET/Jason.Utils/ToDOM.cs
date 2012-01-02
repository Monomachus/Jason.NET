using System;
using System.Xml.Linq;

namespace Jason.Utils
{
	public interface ToDOM {
		XElement getAsDOM(XDocument document);
	}
}
