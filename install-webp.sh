#!/bin/bash

# Installation script for WebP optimization dependencies

echo "Checking for cwebp binary..."

if command -v cwebp &> /dev/null; then
    echo "✓ cwebp is already installed"
    cwebp -version
else
    echo "Installing WebP tools..."
    
    # Detect OS and install accordingly
    if [[ "$OSTYPE" == "linux-gnu"* ]]; then
        if command -v apt-get &> /dev/null; then
            sudo apt-get update
            sudo apt-get install -y webp
        elif command -v yum &> /dev/null; then
            sudo yum install -y libwebp-tools
        elif command -v dnf &> /dev/null; then
            sudo dnf install -y libwebp-tools
        else
            echo "Please install webp tools manually for your Linux distribution"
            exit 1
        fi
    elif [[ "$OSTYPE" == "darwin"* ]]; then
        if command -v brew &> /dev/null; then
            brew install webp
        else
            echo "Please install Homebrew and run: brew install webp"
            exit 1
        fi
    else
        echo "Unsupported OS. Please install cwebp manually."
        exit 1
    fi
fi

echo "✓ WebP optimization setup complete!"
