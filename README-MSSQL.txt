

These directions cover the installation of Microsoft SQL Server 2000 to work
with our EDAM-Enchilada software.  They have been tested using the Developer
Edition, but should work with any copy, hopefully.


1. What software do you need?

You will need Enchilada, possibly a Java Runtime Environment, the SQL server
software and a Microsoft library for connecting to the server using Java.
Instructions for installing these exciting software products follow.


2. Determine Whether you Need a New Java Runtime Environment (JRE)

To find out what version of Java is installed on your computer, we will use a
command line program.  Choose "Run..." from the start menu and type "cmd" into
the dialog box this opens up, then click ok.  This should open up the command
prompt.  Now, in this prompt, type "java -version", then press Enter.

Java has 3 different version numbers because Sun is dumb.  The latest version is
Java 2 version 5.0 build 1.5.0.  The last one is the relevant one.  If you have
a 1.5.something version, that's fine and you can skip installing JRE software
(step 3).  If you have 1.4.something or earlier, you need a newer version for
Enchilada to work, so go to step 3.


3. Start Java Runtime Environment downloading (it takes forever)

Go to the website http://java.com/ and click on Download Now (free java
software).  It should take you to a page to download the software, possibly with
several options.  If there is more than one, you want the Offline Installation
option.


4. Install MS SQL Server

a. The software itself comes in a DVD case with a license key on the back,
which is probably in a box with a bunch of books you won't need (as far as
Enchilada is concerned).  Only one of the discs is needed.  It's probably the
top one: it says Microsoft SQL Server 2000 (something) Edition on the bottom.
It's not the Reporting Services disc and it's not the Service Pack disc.  Put
it in the CD drive of the computer, and open the CD drive from My Computer.
The first screen you see lists several tasks.  You want to install the "SQL
Server 2000 Component" "Database Server".


b. Most of the install you'll be choosing the default option, so this won't be
too hard.  I'll list good choices for each screen.  You want to install on the
_Local Computer_.  Create a _new instance_.  Enter your company name and such
if you like.  Agree to the icky little EULA.  Enter the license key.  Choose to
install _Server and Client Tools_.  Do a _Default_ installation.  Choose a
_Typical_ installation.


c. OK!  We have something nonobvious, finally.  Under the Service Settings
group, we need to choose a local or domain account.  If you work in a place
with lots of tightly integrated windowsy servers, you might want to install the
server using a Domain Account.  Otherwise, you probably want to install with a
Local System account.  Unfortunately, I don't really have an example of when
you would use either one, or how to tell which one to use.  A good thing to do
would be to ask a system administrator or friendly computery person what they
think.  Tell them that the server will only have to talk to programs on its
same computer.

For all that, it might not even make a difference.  Aieee!  I chose Local
System.


d. Choose Mixed Mode authentication so that there's no account on your system
with a really dumb username and password, like we have to have (we'll have to
create a database user for the program).  Choose a password for the system
administrator account.

Click next a couple more times and it should start copying files back and forth
happily.


5. Create a user account for Enchilada.

From the Microsoft SQL Server start menu group open Enterprise Manager.  Open up
the hierarchy view on the left until you find a server that starts in (local).
That's the one you just installed, yay!  Open it up by clicking the plus.  It
will take a while, don't worry.  Open up its Security folder.  Right click on
Logins, and select "New Login...".  Name the user, taking care to keep these
upper- and lower-case letters: SpASMS (which is an old name for this software).
Under Authentication, choose SQL Server authentication, with the password
"finally" (no quotes).  Under the Server Roles pane, check Database Creators and
Bulk Insert Administrators.  Click OK.


6. Install the JDBC library for connecting Enchilada to the database.

In a browser, go to microsoft.com and enter "jdbc sql server 2000" in the
search box in the upper right.  In the results page, you will find the software
we need.  The title is "Download details: SQL Server 2000 Driver for JDBC
Service Pack 1".  Any service pack is fine, though getting the newest one (3 as
of August 2005) is probably a good idea.  To find the other service packs, go
to the very bottom of the "Download details" page you've just opened, look for
the What Others are Downloading section, and open the "SQL Server 2000 Driver
for JDBC Service Pack 3" therein.  From that page, download "setup.exe" and run
it.  There is nothing tricky in its installation.


7. Install the Java Runtime Environment If You Need It.

It's hopefully done downloading by now, so open the installer and install it.
The install is pretty straightforward.


8. Install Enchilada.

Open the Enchilada installer.  It proceeds pretty strightforwardly.  Once it's
done, open the desktop or start menu shortcut and everything should work!!!

 -thomas smith, 15 September 2005




****MSDE*****
A good resource on msde is at:
http://msdn.microsoft.com/library/default.asp?url=/library/en-us/dnmsde/html/msderoadmap.asp

the file you download from microsoft ("MSDE Release A") extracts an installer
and then you have to run the installer yourself from the command line (this
could easily be automated in our install).  The readme file explains the
options.  I got it to work with options like this:

(change to the directory that you extracted to, then:)
setup SAPWD="sa-account-password" TARGETDIR="C:\program files\enchilada\db\" DATADIR="C:\program files\enchilada\db\" DISABLENETWORKPROTOCOLS=0

This only works with that command line on a machine WITHOUT regular ol' SQL
Server installed.  If SQL Server is installed, you have to give a new "instance
name" to the MSDE database server (theoretically, 16 different instances can
coexist on a single system).  And I don't know how to get Enchilada to talk to
a database that is named (so that it is not the Default Database).


Then we need to set up the SpASMS account. To do this, first we have to make
sure it's running.  Open "Service Manager" or "Services" from the Control Panel
(possibly under Maintenance or Administrative Tools or something), and find
SQLServer or MSSQL or something, and click on it, and click start.

From the command line, run:

osql -U sa

Which gives you an SQL shell after you type in the sysadmin password you
entered above in the SAPWD variable.  You type SQL commands and then "go" to
execute them.  Run these commands:

EXEC sp_addlogin 'SpASMS','finally'
EXEC sp_addsrvrolemember 'SpASMS','bulkadmin'
EXEC sp_addsrvrolemember 'SpASMS','dbcreator'
GO

Now SpASMS should be set up and ready to go.  Remember to install the JDBC
drivers, and all should be well and running and stuff!

-thomas, 25 August 2005

get user to type in the sa password, and mess with it ourselves.

alternate:
osql -U sa -P "sapwd" -Q "EXEC sp_addlogin 'SpASMS','finally' EXEC sp_addsrvrolemember 'SpASMS','bulkadmin' EXEC sp_addsrvrolemember 'SpASMS','dbcreator'"

5:45 8:00
7:45 10:15
