"""student management system"""

import os
import sys
from ui.cli import CLI


def main():
    """program entry point"""
    try:
        cli = CLI()
        cli.start()
    except KeyboardInterrupt:
        print("\nExiting program...")
        sys.exit(0)
    except Exception as e:
        print(f"Error: {str(e)}")
        sys.exit(1)


if __name__ == "__main__":
    main()
