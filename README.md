<!DOCTYPE html>
<html>
    <header>
        <img align="right" src="https://i.imgur.com/SWDen2V.png" height="220" width="220">
        <h1>EK</h1>
        <p><b>Complete and configurable music, currency and games multipurpose Discord bot!</b></p>
    </header>
    <body>
        <hr>
        <h4>Check the 4.9 branch for the current development status</h4>
        <hr>
        <h2>Using the Official EK Bot</h2>
        <p><b><i>Just one <a href="http://nothing.nothing">click</a> and you can add EK to your own server and enjoy its full feature set!</i></b></p>
        <hr>
        <h2>Building EK on your own</h2>
        <p><b>WARNING</b>: The owners of EK do not</b> recommend compiling Mantaro as it is not documented and most builds here will be extremely unstable and (probably) untested, probably including unfinished features.<br>If you still want to build your own instance of Mantaro, you will need multiple api keys including <b>(but not limited to)</b></p> 
        <ul>
            <li>osu! API</li>
            <li>AniList API</li>
            <li>OpenWeatherMap API.</li>
            <li>Wolke's Weeb API (For most of the action commands).</li>
            <li>etc...</li>
        </ul>
        <p><b>We will not help you with the process of obtaining said api keys!</b></p>
        <br>
        <p><b>Steps for building</b></p>
        <ol>
            <li>Clone this repository.</li>
            <li>If you are going to edit code, make sure your IDE supports <a href="http://projectlombok.org">Lombok</a> and enable Annotation Processing!</li>
            <li>Open a Terminal in the root folder.</li>
            <li>Run <code>gradlew shadowJar</code></li>
            <li>Grab the <code>-all.jar</code> jar from <code>build/libs</code></li>
            <li>Install <code>rethinkdb</code> and <code>redis</code></li>
            <li>Create the <code>mantaro</code> database with the following tables: mantaro, players, users, guilds, keys, commands</li>
            <li>Run it and prepare yourself to start filling in configs (open the jar on the command line using java -jar name.jar and wait for it to crash, then it'll generate the config.json file for you to fill).</li>
            <li>In config.json, set the value needApi to false.</li>
        </ol>
        <hr>
        <h2>EK Uses and loves</h2>
        <ul>
            <li><a href="https://github.com/DV8FromTheWorld/JDA">JDA by DV8FromTheWorld and MinnDevelopment</a></li>
            <li><a href="https://github.com/sedmelluq/lavaplayer">Lavaplayer by sedmelluq</a></li>
            <li><a href="http://rethinkdb.com">RethinkDB by the RethinkDB team</a></li>
            <li><a href="https://redis.io">Redis by the redis team.</a></li>
            <li><a href="https://github.com/redisson/redisson">Redisson by the redisson team.</a></li>
            <li>And a lot more!</li>
        </ul>
        <hr>
        <p>Give credit where credit is due. If you wish to use our code in a project, <b>please</b> credit us, and take your time to read our full license. We don't mind you using Mantaro code, <b>as it is</b> open-source for a reason, as long as you don't blatantly copy it or refrain from crediting us.</p>
        <h2>License</h2>
        <p>Copyright (C) 2016-2018 <b>David Alejandro Rubio Escares</b>/<b>Kodehawa</b></p>
        <code>
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
You should have received a copy of the GNU General Public License
along with this program.  If not, see http://www.gnu.org/licenses/
        </code>
        <br>
        <a href="https://github.com/Kodehawa/MantaroBot/blob/development/LICENSE">The full license can be found here.</a>
    </body>
</html>
