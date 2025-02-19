# MARS Assembler

[MARS][1], MIPS Assembler and Runtime Simulator, is a lightweight interactive development environment (IDE) for programming in MIPS assembly language, intended for educational-level use with Patterson and Hennessy's Computer Organization and Design.

MARS has been jointly developed by [Pete Sanderson][4] (programming) and [Ken Vollmar][5] (details and paperwork).

This is a fork of [qaisjp/myMARS](https://github.com/qaisjp/myMARS) which contains fixes and nifty additions (like UTF-16 hints), with changes merged from [saagarjha/MARS](https://github.com/saagarjha/MARS) which contains Mac-specific fixes, and [aeris170/MARS-Theme-Engine](https://github.com/aeris170/MARS-Theme-Engine) which contains a theme engine, and personal changes added on to add back Windows compatibility.

The following is a comprehensive list of things this fork attempts to improve upon MARS V4.5:

- Use native macOS menu bar for actions instead of clunky Java UI (from saagarjha's fork).
  - <img alt="Image showing macOS menu bar" src="https://i.imgur.com/nRV9XgN.png" height="200" />
- Also shown above: a Reload menu button to reload the open file without closing and opening again. (from saagarjha's fork)
- Change hardcoded Launch Screen shown time from 2 seconds to 250ms (from qaisjp's fork)
- Fix the dock icon not showing, left is new (from saagarjha's fork)
  - <img alt="Image showing dock icon" src="https://i.imgur.com/SftSNZR.png" height="100px" />
- Add `@Deprecated` to all deprecated functions, removing the warnings during compilation (from saagarjha's fork).
- Use the native file dialog for macOS, left is new (from saagarjha's fork)
  - ![Image showing native file opener](https://i.imgur.com/xdwFFId.png)
- Show UTF-16 encoding of register values (from qaisjp's fork)
- Add "CreateMarsApp.sh", which creates an .app package for macOS to copy to your Applications folder (from saagarjha's fork)
- Fix Windows "CreateMarsJar.bat" not working, and make output cleaner.
- Add ability to select background, line highlight, and selection color (from aeris170's fork).
- Add ability to select foreground color.
- Add a Theme Engine, and the ability to select from a wide variety of common color themes (from aeris170's fork).
  - <img alt="Image showing darkness" src="https://i.imgur.com/hZyeH2i.png" height="300"/>
- And a whole lot of other fixes by Vidminas, qaisjp, JonMoncrieff, and ZayadNimrod, from qaisjp's fork.

## Roadmap

- Add a Recently Opened file list
- Restore open files upon quitting and reopening
- Synchronise editor colors with the selected theme

## Purpose of this repo

This project is a fork of MARS 4.5. Pull requests are very much welcomed.

## Documentation (included in the repo)

 - Go to the [documentation][7].
 - In order to run or compile this project **you must have JRE/JDK 11 (or later) installed on your computer**.

## How to run MARS

- **Desktop**:
  - Save the jar file on the desktop
  - Run MARS by double-clicking the icon

- **Command line**:
  - Save the jar file in some folder
  - Rename the jar file to "Mars.jar" for convenience
  - Open a command line shell in that folder
  - Run MARS by executing `./RunMars.sh` (If you can't due to permissions, run `chmod +x RunMars.sh`)

## How to compile

- **Windows**: execute "CreateMarsJar.bat" file to generate an executable.
- **GNU/Linux** and **Mac**: execute "CreateMarsJar.sh" to generate a java archive. If you can't due to permissions, run `chmod +x CreateMarsJar.sh`.
- **Mac App**: you can also package MARS as a macOS Application package (.app).
  - Install XCode.
  - Clone [saagarjha/GenerateAppIcons](https://github.com/saagarjha/GenerateAppIcons) somewhere.
  - Without cd-ing into GenerateAppIcons, run `xcodebuild -project GenerateAppIcons/GenerateAppIcons.xcodeproj/ -scheme GenerateAppIcons CODE_SIGNING_ALLOWED=NO`.
  - Now you can use "CreateMarsApp.sh" to generate an App package that you can copy to your Applications folder.

## How to release

- Create new folder `mkdir myMARS`
- Run `./CreateMarsJar.sh`
- Copy files `cp Mars.jar RunMars.sh`
- Create zip `zip myMARS.zip myMARS`
- Release `myMARS.zip` with `vYYYY-MM-DD` as the version tag

## License

[MIT][2]: Check the [LICENSE.md][3] file for detailed licensing text.

  [1]: http://courses.missouristate.edu/KenVollmar/MARS/index.htm
  [2]: http://www.opensource.org/licenses/mit-license.html
  [3]: https://github.com/yutotakano/myMARS/blob/master/LICENSE.md
  [4]: http://faculty.otterbein.edu/PSanderson/
  [5]: http://courses.missouristate.edu/KenVollmar/
  [6]: http://courses.missouristate.edu/KenVollmar/MARS/download.htm
  [7]: http://courses.missouristate.edu/KenVollmar/MARS/Help/MarsHelpIntro.html
