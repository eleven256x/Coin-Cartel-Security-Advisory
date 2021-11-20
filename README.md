# Coin Cartel Account Security Vulnerability
> When trusting your software vendor goes horribly wrong! Wondering if other people may be doing the same thing? Contact us privately via DMs!
> <br>
## Read this before you continue:
> **Legal Disclaimer**: There are no acts to defame or libel anyone as all accusations are supported by solid-proof evidence.
> <br>
> **File Takedown**: Please contact me privately to resolve things before issues get out of hand, This was done for good faith.
> <br>
> **Video Demonstration**: [Security Advisory Video](https://youtu.be/BWeS0avpmNk)
> 
### Details:
  > **Discovery Date**: 11/14/2021
  > <br>
  > **Discovery By**: eleven#1337
  > <br>
  > **Disclosure**: 11/17/2021
  > <br>
  > **Severity**: High
 
### Vulnerability Scope:
> This vulnerability affects all Coin Cartel Paid & Crack users using any related Coin Cartel Scripting Product(s).

### Description:
> This vulnerability was made possible by Coin Cartel Management ("Blitzkrieg/Blitz") after pushing malicious code to production near the original release of 0.0.4_(build).zip (9/20/2021), Though we cannot confirm the original beginnings of the vulnerability it is a violation of user trust and privacy. This allows the attacker in this case ("Coin Cartel") the ability to have full unlimited access to your minecraft account until your Session ID is invalidated. This vulnerability affects everyone using the Paid & Cracked verisons of Coin Cartel Scripting Product(s) due to the naturally ability of websockets and it's willingness to accept any connections as long as it's online.

### Reproduction
> **How is this possible?** This vulnerability is caused by a malicious module dynamically loaded when authenticated with there servers.
> <br>
> <br>
> **To reproduce this vulnerability, The attacker needs to follow the following steps**:
> <br>
> **1**. Create a malicious module to capture Session IDs and pass them as authorization tokens to a Websocket Server.
> <br>
> **2**. Create a Websocket Server to take in connections.
> <br>
> **3**. Load the module by dragging it into your "liteconfig/macros/modules" folder or via "custom moduleloader".
> <br>
> <br>
> **Information**: All files seen/used within the posted [video](https://www.youtube.com/watch?v=BWeS0avpmNk) are posted with this repository, You can view them by going to the original directory.

### Resolution
> This vulnerability cannot be fixed for paid versions unless you break Coin Cartel Terms of Service, Though for the Cracked Version it's a different story.

### Mitigation
> There are no fool-proof plans for the paid version but another story for the cracked version, Though I won't disclose how to manually patch these files to disable the module I would recommend you guys to stop using any assorted Coin Cartel Scripting Product(s) until I develop a viable solution though the likeliness is slim.

### Takeaway:
> Coin Cartel as a software company failed to provide clean functional programs to it's already massive community. Violating simple user trust and privacy I now heavily believe you should stay away from this project due to the owner's dysfunctional operational abilities.
> <br>
> <br>
> Please be safe when purchasing software or things like this may arise again in the future, Then again. Thank you for reading this Security Advisory on Coin Cartel, I hope you have a wonderful day.

### Contact us & Discussion:
> Want to reach me? You can contact me at eleven#1337
> <br>
> Want to discuss with other people about the topic? Join our at [Discord Server](https://discord.gg/StJcrgxrqR)!
