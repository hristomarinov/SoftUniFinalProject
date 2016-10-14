# SoftUniFinalProject

 “SoftUniProject” is application for reading news and watching video news. When it starts, we see RecyclerView List with the articles. The application works with two databases. Firs is for the Web API to get the news, second is SQLite database to store user accounts (registrations) and the favourite articles.
The application provide a possibility to Login, Register and Profile. This Fragments are connected to the SQLite and the information is saved. The user password is encrypted.
In the Profile fragment has option to change the profile photo. There are two options.
1) To take image from the phone.  2) To take Photo from the Camera.
 
There is a BroadcastReceiver which detects the network change. In the Favourite list we can swipe-left / swipe-right to delete the article from favorite list. 
When some article is added to Favourites there is a Service which creates notification (with and click event and PendingIntent to open the article).
In the list of with the article. 
LongClick event is also implemented in the MainActivity. Its main functions is shows a popup window tho options:
1) Open article. 
2) Add to Favourites.
Favourites list is also able to reorder the articles with DoubleTouch Drag. 
