# PowerShell script to clear port 8080 and restart backend

Write-Host "Checking for processes on port 8080..." -ForegroundColor Cyan

# Find process using port 8080
$processInfo = netstat -ano | findstr :8080 | Select-Object -First 1

if ($processInfo)
{
    # Extract PID from the output
    $processId = ($processInfo -split '\s+')[-1]
    
    Write-Host "Found process on port 8080 (PID: $processId)" -ForegroundColor Yellow
    Write-Host "Killing process..." -ForegroundColor Yellow
    
    try
    {
        Stop-Process -Id $processId -Force -ErrorAction Stop
        Write-Host "Process killed successfully!" -ForegroundColor Green
        Start-Sleep -Seconds 2
    }
    catch
    {
        Write-Host "Failed to kill process: $_" -ForegroundColor Red
        exit 1
    }
}
else
{
    Write-Host "Port 8080 is already free!" -ForegroundColor Green
}

Write-Host ""
Write-Host "🚀 Starting Spring Boot application..." -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray
Write-Host ""

# Start the backend
& .\mvnw.cmd spring-boot:run
