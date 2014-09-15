using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Security.Space
{
    [Serializable]
    public class TokenMessage
    {
        public String Token { get; set; }

        public TokenMessage() { }

        public TokenMessage(byte[] token)
        {
            this.Token = BitUtils.ByteArrayToString(token);
        }

        public override string ToString()
        {
            return base.ToString() + ":" + Token;
        }
    }
}
