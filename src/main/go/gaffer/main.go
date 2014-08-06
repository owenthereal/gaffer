package main

import (
	"path/filepath"

	"github.com/jingweno/jshim"
)

func main() {
	shim := jshim.New("-jar", filepath.Join("lib", "gaffer.jar"))
	shim.Execute()
}
