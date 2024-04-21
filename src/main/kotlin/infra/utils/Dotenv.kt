package infra.utils

import java.io.File

class Dotenv {
	fun load(): String {
		try	{
			File("./.env").forEachLine {
				if (it.isNotEmpty()) {
					if (it.substring(0, 1) == "#") {
						//println("Comment: $it")
					} else {
						val idx = it.indexOf("=")

						if (idx >= 0) {
							val k: String = it.substring(0, idx)
							val v: String = it.substring(idx + 1)
							System.setProperty(k, v)
						}
					}
				}
			}
		} catch (e: Exception) {
			//println(e)
		}

		return "ok"
	}
}
