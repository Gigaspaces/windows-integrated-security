using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Xml.Serialization;

namespace Security.Space
{
    public class Message
    {

        [XmlElement(typeof(HelloMessage), ElementName = "HelloMessage")]
        [XmlElement(typeof(AuthorizationRequiredMessage), ElementName = "AuthorizationRequiredMessage")]
        [XmlElement(typeof(AuthorizedMessage), ElementName = "AuthorizedMessage")]
        [XmlElement(typeof(NotAuthorizedMessage), ElementName = "NotAuthorizedMessage")]
        [XmlElement(typeof(TokenMessage), ElementName = "TokenMessage")]
        public Object Body { get; set; }

        public Message() { }

        public Message(object body)
        {
            this.Body = body;
        }
    }
}
