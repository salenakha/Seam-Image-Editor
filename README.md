# Seam Carving Image Editor

A collaboratively-developed Java application that implements content-aware image resizing using "seam carving" algorithms. Removes vertical pixel seams from images while preserving "important" visual content.

## Features

- **Lowest Energy Seam Removal**: Identifies and removes seams with minimal visual impact using Sobel edge detection
- **Greenest Seam Removal**: Removes vertical seams with highest green color intensity
- **Undo Functionality**: Restore previously removed seams using Command pattern
- **Interactive CLI**: Simple command-line interface with confirmation prompts

## Process

The algorithm uses dynamic programming to find optimal vertical seams:
1. Calculate energy for each pixel based on neighboring pixels
2. Build seam paths from top to bottom, maximizing/minimizing cumulative values
3. Remove seam by adjusting doubly-linked pixel structure
4. Support undo by reinserting saved seam data

**Key Feature**: Custom linked-list structure enables O(1) seam removal per row (vs O(n) for arrays).

For detailed technical explanation, see my [Project Report](./docs/AE3_Project_Report.pdf)!

## Usage

### Running the Application
```bash
javac -d bin src/main/java/uk/ac/nulondon/*.java
java -cp bin uk.ac.nulondon.Main
```

### Commands
```
g - Remove greenest seam
e - Remove lowest energy seam
u - Undo previous edit
q - Quit and save
```

### Example
```
Welcome! Enter file path
> src/main/resources/beach.png

Please enter a command
> e
Remove a lowest energy seam. Continue? (Y/N)
> y
```

Output saved to `target/newImg.png`

## Sample Results

Intermediate states saved as `target/tmp1.png`, `target/tmp2.png`, etc.

## Testing
```bash
mvn test
```

Tests include:
- Pixel brightness calculation
- Seam highlighting and removal
- Image integrity verification

## Requirements

**To run**: Java 17+

**To run tests**: JUnit 5, AssertJ, ApprovalTests

## Technical Details

- **Time Complexity**: O(W×H) for energy calculation and seam finding
- **Space Complexity**: O(W×H) for image storage
- **Design Pattern**: Command pattern for undo/redo

---

**Course**: Northeastern University London: Fundamentals of Computer Science 2 (Java)
**Date**: April 2025
